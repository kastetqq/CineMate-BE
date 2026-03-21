package com.ratingapp.reviews.repository;

import com.ratingapp.reviews.entity.ReviewReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewReactionRepository extends JpaRepository<ReviewReaction, UUID> {
    
    Optional<ReviewReaction> findByUserIdAndReviewId(UUID userId, UUID reviewId);
    
    boolean existsByUserIdAndReviewId(UUID userId, UUID reviewId);
    
    void deleteByUserIdAndReviewId(UUID userId, UUID reviewId);
    
    long countByReviewId(UUID reviewId);
}