package com.ratingapp.reviews.repository;

import com.ratingapp.reviews.entity.ReviewFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewFavoriteRepository extends JpaRepository<ReviewFavorite, UUID> {
    
    Optional<ReviewFavorite> findByUserIdAndReviewId(UUID userId, UUID reviewId);
    
    boolean existsByUserIdAndReviewId(UUID userId, UUID reviewId);
    
    @Query("SELECT f FROM ReviewFavorite f WHERE f.user.id = :userId ORDER BY f.review.createdAt DESC")
    Page<ReviewFavorite> findByUserIdOrderByReviewCreatedAtDesc(UUID userId, Pageable pageable);
}