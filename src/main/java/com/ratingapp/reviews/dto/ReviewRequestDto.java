package com.ratingapp.reviews.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewRequestDto {
    private Long movieId;
    private String movieTitle;
    private String moviePosterPath;
    private LocalDateTime movieReleaseDate;
    private String content;
    private Integer rating;
    private Boolean isSpoiler;
}