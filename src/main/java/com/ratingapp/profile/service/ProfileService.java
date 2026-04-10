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
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {
    
    private final UserService userService;
    private final AvatarService avatarService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    
    public UserProfileDto getUserProfile(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setBio(user.getBio());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setRole(user.getRole().name());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
    
    public UserProfileDto getProfileById(UUID id) {
        User user = userService.findById(id);
        return getUserProfile(user);
    }

    @Transactional
    public UserProfileDto updateUsername(User user, String newUsername) {
        if (newUsername == null || newUsername.trim().length() < 3) {
            throw new RuntimeException("Username must be at least 3 characters");
        }
        if (userService.findByUsername(newUsername).isPresent()) {
            throw new RuntimeException("Username already taken");
        }
        
        user.setUsername(newUsername);
        User updated = userService.save(user);
        return getUserProfile(updated);
    }

    @Transactional
    public UserProfileDto updateEmail(User user, String newEmail) {
        if (!newEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new RuntimeException("Invalid email format");
        }
        if (userService.findByEmail(newEmail).isPresent()) {
            throw new RuntimeException("Email already taken");
        }
        
        user.setEmail(newEmail);
        User updated = userService.save(user);
        return getUserProfile(updated);
    }

    @Transactional
    public UserProfileDto updateBio(User user, String bio) {
        if (bio != null && bio.length() > 500) {
            throw new RuntimeException("Bio must be less than 500 characters");
        }
        
        user.setBio(bio);
        User updated = userService.save(user);
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
        
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userService.save(user);
        log.info("Password updated for user: {}", user.getId());
    }
    
    @Transactional
    public UserProfileDto updateAvatar(User user, MultipartFile file) {
        String newAvatarUrl = avatarService.uploadAvatar(file, user.getId());
        
        String oldAvatarUrl = user.getAvatarUrl();
        user.setAvatarUrl(newAvatarUrl);
        User updated = userService.save(user);
        
        if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
            avatarService.deleteAvatar(oldAvatarUrl);
        }
        
        log.info("Avatar updated for user: {}", user.getId());
        return getUserProfile(updated);
    }
    
    @Transactional
    public void deleteAvatar(User user) {
        String avatarUrl = user.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            user.setAvatarUrl(null);
            userService.save(user);
            avatarService.deleteAvatar(avatarUrl);
            log.info("Avatar deleted for user: {}", user.getId());
        }
    }
}