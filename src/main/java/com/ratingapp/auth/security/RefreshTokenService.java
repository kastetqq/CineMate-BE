package com.ratingapp.auth.security;

import com.ratingapp.auth.entity.RefreshToken;
import com.ratingapp.auth.entity.User;
import com.ratingapp.auth.repository.RefreshTokenRepository;
import com.ratingapp.auth.repository.UserRepository;
import com.ratingapp.auth.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@Transactional
public class RefreshTokenService {
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Autowired
    private JwtService jwtService;
    
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, 
                             UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public String storeRefreshToken(String refreshToken, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String userId = user.getId().toString();
        
        String tokenSignature = generateTokenSignature(refreshToken);
        
        LocalDateTime expiresAt = jwtService.getExpirationDateFromToken(refreshToken)
                .toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime();
        
        RefreshToken entity = new RefreshToken(tokenSignature, userId, expiresAt);
        refreshTokenRepository.save(entity);
        
        return tokenSignature;
    }

    public boolean isValidRefreshToken(String refreshToken) {
        try {
            String tokenSignature = generateTokenSignature(refreshToken);
            Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByTokenSignature(tokenSignature);
            
            if (tokenOpt.isEmpty()) {
                return false;
            }
            
            RefreshToken token = tokenOpt.get();
            boolean isValid = !token.isRevoked() && token.getExpiresAt().isAfter(LocalDateTime.now());
            boolean isJwtValid = jwtService.validateJwtToken(refreshToken) && 
                            jwtService.isRefreshToken(refreshToken);
            
            return isValid && isJwtValid;
            
        } catch (Exception e) {
            return false;
        }
    }

    public void revokeRefreshToken(String refreshToken) {
        String tokenSignature = generateTokenSignature(refreshToken);
        refreshTokenRepository.findByTokenSignature(tokenSignature)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }
    
    public void revokeAllUserTokens(String userId) {
        refreshTokenRepository.findByUserId(userId)
                .forEach(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    public void revokeAllUserTokensByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String userId = user.getId().toString();
        revokeAllUserTokens(userId);
    }

    private String generateTokenSignature(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash).substring(0, 44);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating token signature", e);
        }
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}