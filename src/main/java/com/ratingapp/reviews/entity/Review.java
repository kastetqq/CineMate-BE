package com.ratingapp.reviews.entity;

import com.ratingapp.auth.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "movie_id", nullable = false)
    private Long movieId;
    
    @Column(name = "movie_title", nullable = false)
    private String movieTitle;
    
    @Column(name = "movie_poster_path")
    private String moviePosterPath;
    
    @Column(name = "movie_release_date")
    private LocalDateTime movieReleaseDate;
    
    @Column(nullable = false, length = 2000)
    private String content;
    
    @Column(nullable = false)
    private Integer rating;
    
    @Column(name = "likes_count")
    private Integer likesCount = 0;
    
    @Column(name = "comments_count")
    private Integer commentsCount = 0;
    
    @Column(name = "is_spoiler")
    private Boolean isSpoiler = false;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}