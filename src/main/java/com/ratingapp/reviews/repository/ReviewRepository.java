package com.ratingapp.reviews.repository;

import com.ratingapp.reviews.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    
    Optional<Review> findByUserIdAndMovieId(UUID userId, Long movieId);
    
    Page<Review> findByMovieIdOrderByCreatedAtDesc(Long movieId, Pageable pageable);
    
    Page<Review> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    
    Page<Review> findByMovieTitleContainingIgnoreCaseOrderByCreatedAtDesc(String movieTitle, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE LOWER(r.user.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    Page<Review> findByUserNameContaining(@Param("username") String username, Pageable pageable);
}