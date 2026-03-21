package com.ratingapp.reviews.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReviewResponseDto {
    private UUID id;
    private UUID userId;
    private String userName;
    private Long movieId;
    private String movieTitle;
    private String moviePosterPath;
    private LocalDateTime movieReleaseDate;
    private String content;
    private Integer rating;
    private Integer likesCount;
    private Integer commentsCount;
    private Boolean isSpoiler;
    private LocalDateTime createdAt;
    private Boolean isLikedByCurrentUser;
    private Boolean isFavoritedByCurrentUser;
}