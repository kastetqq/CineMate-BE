package com.ratingapp.profile.controller;

import com.ratingapp.auth.entity.User;
import com.ratingapp.profile.dto.*;
import com.ratingapp.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    
    private final ProfileService profileService;
    
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getProfile(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(profileService.getUserProfile(user));
    }
    
    @PutMapping("/username")
    public ResponseEntity<UserProfileDto> updateUsername(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateUsernameRequest request) {
        return ResponseEntity.ok(profileService.updateUsername(user, request.getUsername()));
    }
    
    @PutMapping("/bio")
    public ResponseEntity<UserProfileDto> updateBio(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateBioRequest request) {
        return ResponseEntity.ok(profileService.updateBio(user, request.getBio()));
    }
    
    @PutMapping("/email")
    public ResponseEntity<UserProfileDto> updateEmail(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateEmailRequest request) {
        return ResponseEntity.ok(profileService.updateEmail(user, request.getNewEmail()));
    }
    
    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal User user,
            @RequestBody UpdatePasswordRequest request) {
        profileService.updatePassword(user, request);
        return ResponseEntity.ok().build();
    }
}