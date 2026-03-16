package com.ratingapp.profile.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserProfileDto {
    private UUID id;
    private String username;
    private String email;
    private String bio;
    private String role;
    private LocalDateTime createdAt;
}