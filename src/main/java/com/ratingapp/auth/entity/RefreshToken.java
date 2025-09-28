package com.ratingapp.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, unique = true, length = 64)
    private String tokenSignature;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(nullable = false)
    private boolean revoked = false;
    
    public RefreshToken() {}
    
    public RefreshToken(String tokenSignature, String userId, LocalDateTime expiresAt) {
        this.tokenSignature = tokenSignature;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getTokenSignature() { return tokenSignature; }
    public void setTokenSignature(String tokenSignature) { this.tokenSignature = tokenSignature; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
}