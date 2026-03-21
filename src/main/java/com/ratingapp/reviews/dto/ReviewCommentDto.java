package com.ratingapp.reviews.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ReviewCommentDto {
    private UUID id;
    private UUID userId;
    private String userName;
    private String content;
    private Integer likesCount;
    private LocalDateTime createdAt;
    private List<ReviewCommentDto> replies;
}