package com.ratingapp.profile.service;

import com.ratingapp.auth.entity.User;
import com.ratingapp.auth.service.UserService;
import com.ratingapp.profile.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    
    public UserProfileDto getUserProfile(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setBio(user.getBio());
        dto.setRole(user.getRole().name());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
    
    @Transactional
    public UserProfileDto updateUsername(User user, String newUsername) {
        if (newUsername == null || newUsername.trim().length() < 3) {
            throw new RuntimeException("Username must be at least 3 characters");
        }
        User updated = userService.updateUsername(user.getId(), newUsername);
        return getUserProfile(updated);
    }
    
    @Transactional
    public UserProfileDto updateBio(User user, String bio) {
        if (bio != null && bio.length() > 500) {
            throw new RuntimeException("Bio must be less than 500 characters");
        }
        User updated = userService.updateBio(user.getId(), bio);
        return getUserProfile(updated);
    }
    
    @Transactional
    public UserProfileDto updateEmail(User user, String newEmail) {
        if (!newEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new RuntimeException("Invalid email format");
        }
        
        // Мгновенная смена email без подтверждения!
        User updated = userService.updateEmail(user.getId(), newEmail);
        return getUserProfile(updated);
    }
    
    @Transactional
    public void updatePassword(User user, UpdatePasswordRequest request) {
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Old password is incorrect");
        }
        
        if (request.getNewPassword().length() < 6) {
            throw new RuntimeException("New password must be at least 6 characters");
        }
        
        String hash = passwordEncoder.encode(request.getNewPassword());
        userService.updatePassword(user.getId(), hash);
        log.info("Password updated for user: {}", user.getId());
    }
}