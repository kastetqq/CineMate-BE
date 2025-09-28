package com.ratingapp.auth.controller;

import com.ratingapp.auth.dto.*;
import com.ratingapp.auth.security.RefreshTokenService;
import com.ratingapp.auth.security.jwt.JwtService;
import com.ratingapp.auth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;
    
    public AuthController(UserService userService, 
                        JwtService jwtService, 
                        AuthenticationManager authenticationManager,
                        UserDetailsService userDetailsService,
                        RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/sign-out")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, 
                                                 @RequestBody(required = false) Map<String, String> body) {
    String authHeader = request.getHeader("Authorization");
    
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Collections.singletonMap("error", "Authorization header is required"));
    }
    
    String accessToken = authHeader.substring(7);
    
    if (!jwtService.validateJwtToken(accessToken) || !jwtService.isAccessToken(accessToken)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Collections.singletonMap("error", "Invalid or expired token"));
    }
    
    try {
        String email = jwtService.getEmailFromToken(accessToken);
        
        jwtService.blacklistAccessToken(accessToken);

        refreshTokenService.revokeAllUserTokensByEmail(email);

        if (body != null && body.containsKey("refreshToken")) {
            String refreshToken = body.get("refreshToken");
            if (refreshToken != null && !refreshToken.isBlank()) {
                refreshTokenService.revokeRefreshToken(refreshToken);
            }
        }
        
        SecurityContextHolder.clearContext();
        
        return ResponseEntity.ok(Collections.singletonMap("message", "Logged out successfully"));
        
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Collections.singletonMap("error", "Logout failed: " + e.getMessage()));
        }
    }

    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Collections.singletonMap("error", "Authorization header is required"));
    }
    
    String token = authHeader.substring(7);
    
    if (!jwtService.isAccessToken(token)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Collections.singletonMap("error", "Only access tokens are allowed for this endpoint"));
    }
    
    if (jwtService.isAccessTokenBlacklisted(token)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Collections.singletonMap("error", "Token has been revoked"));
    }
    
    if (!jwtService.validateJwtToken(token)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Collections.singletonMap("error", "Invalid or expired token"));
    }
    
    try {
        String email = jwtService.getEmailFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        
        Map<String, Object> response = Map.of(
            "email", userDetails.getUsername(),
            "authenticated", true,
            "roles", userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .toArray()
        );
        
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Collections.singletonMap("error", "Failed to get user information"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserCredentialsDto registerRequest) {
        try {
            UserResponseDto userDto = userService.registerUser(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(userDto); 
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody UserCredentialsDto credentials) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    credentials.getEmail(), 
                    credentials.getPassword()
                )
            );
            
            JwtAuthenticationDto jwtDto = jwtService.generateAuthToken(credentials.getEmail());
            return ResponseEntity.ok(jwtDto);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Invalid email or password"));
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenDto refreshRequest) {
        try {
            if (refreshRequest == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("error", "Refresh token data is required"));
            }
            
            String refreshToken = refreshRequest.getRefreshToken();
            
            if (refreshToken == null || refreshToken.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("error", "Refresh token is required"));
            }
            
            if (!jwtService.validateRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "Invalid or expired refresh token"));
            }
            
            String email = jwtService.getEmailFromToken(refreshToken);
            JwtAuthenticationDto newTokens = jwtService.refreshBaseToken(email, refreshToken);
            
            return ResponseEntity.ok(newTokens);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Token refresh failed: " + e.getMessage()));
        }
    }
}