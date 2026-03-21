package com.ratingapp.reviews.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ReviewCommentRequestDto {
    private String content;
    private UUID parentId;
}