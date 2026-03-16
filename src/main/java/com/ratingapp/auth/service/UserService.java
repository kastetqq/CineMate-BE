package com.ratingapp.auth.service;

import com.ratingapp.auth.dto.UserCredentialsDto;
import com.ratingapp.auth.dto.UserResponseDto;
import com.ratingapp.auth.entity.User;
import com.ratingapp.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public User findById(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    @Transactional
    public UserResponseDto registerUser(UserCredentialsDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        
        User savedUser = userRepository.save(user);
        
        UserResponseDto response = new UserResponseDto();
        response.setId(savedUser.getId());
        response.setUsername(savedUser.getUsername());
        response.setEmail(savedUser.getEmail());
        response.setBio(savedUser.getBio());
        response.setRole(savedUser.getRole().name());
        response.setCreatedAt(savedUser.getCreatedAt());
        
        return response;
    }
    
    @Transactional
    public User updateUsername(UUID userId, String newUsername) {
        if (userRepository.existsByUsername(newUsername)) {
            throw new RuntimeException("Username already taken");
        }
        User user = findById(userId);
        user.setUsername(newUsername);
        return userRepository.save(user);
    }
    
    @Transactional
    public User updateEmail(UUID userId, String newEmail) {
        if (userRepository.existsByEmail(newEmail)) {
            throw new RuntimeException("Email already taken");
        }
        User user = findById(userId);
        user.setEmail(newEmail);
        return userRepository.save(user);
    }
    
    @Transactional
    public User updateBio(UUID userId, String bio) {
        if (bio != null && bio.length() > 500) {
            throw new RuntimeException("Bio must be less than 500 characters");
        }
        User user = findById(userId);
        user.setBio(bio);
        return userRepository.save(user);
    }
    
    @Transactional
    public void updatePassword(UUID userId, String newPasswordHash) {
        User user = findById(userId);
        user.setPasswordHash(newPasswordHash);
        userRepository.save(user);
    }
    
    @Transactional
    public void markEmailAsVerified(UUID userId) {
        User user = findById(userId);
        userRepository.save(user);
    }
}