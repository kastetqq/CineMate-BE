package com.ratingapp.auth.security.jwt;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.crypto.SecretKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import com.ratingapp.auth.dto.JwtAuthenticationDto;
import com.ratingapp.auth.security.RefreshTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;

@Component
public class JwtService {

    private static final Logger LOGGER = LogManager.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final RefreshTokenService refreshTokenService;
    
    private final ConcurrentMap<String, Date> blacklistedTokens = new ConcurrentHashMap<>();

    public JwtService(@Lazy RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    public JwtAuthenticationDto generateAuthToken(String email){
        JwtAuthenticationDto jwtDto = new JwtAuthenticationDto();
        jwtDto.setToken(generateJwtToken(email));
        
        String refreshToken = generateRefreshToken(email);
        jwtDto.setRefreshToken(refreshToken);
        
        refreshTokenService.storeRefreshToken(refreshToken, email);
        
        return jwtDto;
    }

    public JwtAuthenticationDto refreshBaseToken(String email, String oldRefreshToken) { 
        if (!refreshTokenService.isValidRefreshToken(oldRefreshToken)) {
            throw new SecurityException("Invalid or revoked refresh token");
        }
        
        refreshTokenService.revokeRefreshToken(oldRefreshToken);
        
        JwtAuthenticationDto jwtDto = new JwtAuthenticationDto();
        jwtDto.setToken(generateJwtToken(email));
        
        String newRefreshToken = generateRefreshToken(email);
        jwtDto.setRefreshToken(newRefreshToken);
        
        refreshTokenService.storeRefreshToken(newRefreshToken, email);
        
        return jwtDto;
    }

    public String getEmailFromToken(String token){
        Claims claims = Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public boolean validateJwtToken(String token){
        try {
            if (isAccessTokenBlacklisted(token)) {
                LOGGER.warn("Attempt to use blacklisted token");
                return false;
            }
            
            Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException error) {
            LOGGER.error("Expired JWT token: {}", error.getMessage());
        } catch (Exception error) {
            LOGGER.error("Error with JWT token: {}", error.getMessage());
        }
        return false;
    }

    public boolean validateRefreshToken(String token) {
        return refreshTokenService.isValidRefreshToken(token);
    }

    public void invalidateRefreshToken(String token) {
        refreshTokenService.revokeRefreshToken(token);
    }

    public void invalidateAllUserTokens(String email) {
        refreshTokenService.revokeAllUserTokensByEmail(email);
    }

    private String generateJwtToken(String email){
        Date date = Date.from(LocalDateTime.now().plusMinutes(1).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .setSubject(email)
                .claim("token_type", "access")
                .setIssuedAt(new Date())
                .setExpiration(date)
                .signWith(getSignInKey())
                .compact();
    }

    private String generateRefreshToken(String email){
        Date date = Date.from(LocalDateTime.now().plusMinutes(3).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .setSubject(email)
                .claim("token_type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(date)
                .signWith(getSignInKey())
                .compact();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration();
        } catch (Exception e) {
            LOGGER.error("Error getting expiration date from token: {}", e.getMessage());
            return null;
        }
    }
    
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration != null && expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    public boolean isAccessToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            String tokenType = (String) claims.get("token_type");
            return "access".equals(tokenType);
            
        } catch (Exception e) {
            LOGGER.error("Error checking token type: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            String tokenType = (String) claims.get("token_type");
            return "refresh".equals(tokenType);
            
        } catch (Exception e) {
            LOGGER.error("Error checking token type: {}", e.getMessage());
            return false;
        }
    }
    
    public void blacklistAccessToken(String token) {
        try {
            String tokenSignature = generateTokenSignature(token);
            Date expiration = getExpirationDateFromToken(token);
            
            if (expiration != null) {
                blacklistedTokens.put(tokenSignature, expiration);
                LOGGER.info("Access token blacklisted, expires at: {}", expiration);
            }
        } catch (Exception e) {
            LOGGER.error("Error blacklisting token: {}", e.getMessage());
        }
    }
    
    public boolean isAccessTokenBlacklisted(String token) {
        try {
            String tokenSignature = generateTokenSignature(token);
            Date expiration = blacklistedTokens.get(tokenSignature);
            
            if (expiration != null) {
                if (expiration.before(new Date())) {
                    blacklistedTokens.remove(tokenSignature);
                    return false;
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.error("Error checking token blacklist: {}", e.getMessage());
            return false;
        }
    }
    
    public void cleanupBlacklistedTokens() {
        Date now = new Date();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().before(now));
        LOGGER.info("Cleaned up blacklisted tokens, remaining: {}", blacklistedTokens.size());
    }
    
    private String generateTokenSignature(String token) {
        try {
            return token.substring(Math.max(0, token.length() - 32));
        } catch (Exception e) {
            LOGGER.error("Error generating token signature: {}", e.getMessage());
            return token; 
        }
    }
    
    public String getTokenType(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return (String) claims.get("token_type");
        } catch (Exception e) {
            LOGGER.error("Error getting token type: {}", e.getMessage());
            return "unknown";
        }
    }
}