package com.ratingapp.auth.repository;

import com.ratingapp.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    
    Optional<RefreshToken> findByTokenSignature(String tokenSignature);
    
    List<RefreshToken> findByUserId(String userId);
    
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.userId = :userId")
    void deleteByUserId(String userId);
    
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.tokenSignature = :tokenSignature")
    void deleteByTokenSignature(String tokenSignature);
    
    boolean existsByTokenSignatureAndRevokedFalse(String tokenSignature);
    
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :currentTime")
    void deleteExpiredTokens(LocalDateTime currentTime);
}