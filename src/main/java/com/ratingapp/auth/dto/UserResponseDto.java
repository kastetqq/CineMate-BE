package com.ratingapp.auth.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class UserResponseDto {
    private UUID id;
    private String username;
    private String email;
    private String avatarUrl;
    private String role;
    private LocalDateTime createdAt;
}
