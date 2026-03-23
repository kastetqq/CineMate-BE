package com.ratingapp.reviews.repository;

import com.ratingapp.reviews.entity.ReviewComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ReviewCommentRepository extends JpaRepository<ReviewComment, UUID> {
    
    Page<ReviewComment> findByReviewIdAndParentIsNullOrderByCreatedAtDesc(UUID reviewId, Pageable pageable);
    
    long countByReviewId(UUID reviewId);
}