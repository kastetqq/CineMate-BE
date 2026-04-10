package com.ratingapp.auth.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserResponseDto {
    private UUID id;
    private String username;
    private String email;
    private String avatarUrl;
    private String bio;
    private String role;
    private LocalDateTime createdAt;
}