package com.ratingapp.reviews.controller;

import com.ratingapp.auth.entity.User;
import com.ratingapp.auth.security.CustomUserDetails;
import com.ratingapp.reviews.dto.*;
import com.ratingapp.reviews.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(@RequestBody ReviewRequestDto request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = ((CustomUserDetails) auth.getPrincipal()).getUser();
        
        return ResponseEntity.ok(reviewService.createReview(request, user));
    }
    
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @PathVariable UUID reviewId,
            @RequestBody ReviewRequestDto request) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = ((CustomUserDetails) auth.getPrincipal()).getUser();
        
        return ResponseEntity.ok(reviewService.updateReview(reviewId, request, user));
    }
    
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID reviewId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = ((CustomUserDetails) auth.getPrincipal()).getUser();
        
        reviewService.deleteReview(reviewId, user);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/all")
    public ResponseEntity<Page<ReviewResponseDto>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        
        if (auth != null && auth.isAuthenticated()) {
            currentUser = ((CustomUserDetails) auth.getPrincipal()).getUser();
        }
        
        return ResponseEntity.ok(reviewService.getAllReviews(page, size, currentUser));
    }
    
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<Page<ReviewResponseDto>> getReviewsByMovie(
            @PathVariable Long movieId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        
        if (auth != null && auth.isAuthenticated()) {
            currentUser = ((CustomUserDetails) auth.getPrincipal()).getUser();
        }
        
        return ResponseEntity.ok(reviewService.getReviewsByMovie(movieId, page, size, currentUser));
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ReviewResponseDto>> getReviewsByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        
        if (auth != null && auth.isAuthenticated()) {
            currentUser = ((CustomUserDetails) auth.getPrincipal()).getUser();
        }
        
        return ResponseEntity.ok(reviewService.getReviewsByUser(userId, page, size, currentUser));
    }
    
    @GetMapping("/search/movie")
    public ResponseEntity<Page<ReviewResponseDto>> searchByMovieTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        
        if (auth != null && auth.isAuthenticated()) {
            currentUser = ((CustomUserDetails) auth.getPrincipal()).getUser();
        }
        
        return ResponseEntity.ok(reviewService.searchByMovieTitle(title, page, size, currentUser));
    }
    
    @GetMapping("/search/user")
    public ResponseEntity<Page<ReviewResponseDto>> searchByUserName(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        
        if (auth != null && auth.isAuthenticated()) {
            currentUser = ((CustomUserDetails) auth.getPrincipal()).getUser();
        }
        
        return ResponseEntity.ok(reviewService.searchByUserName(username, page, size, currentUser));
    }
    
    @PostMapping("/{reviewId}/like")
    public ResponseEntity<Void> likeReview(@PathVariable UUID reviewId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = ((CustomUserDetails) auth.getPrincipal()).getUser();
        
        reviewService.likeReview(reviewId, user);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{reviewId}/favorite")
    public ResponseEntity<Void> addToFavorites(@PathVariable UUID reviewId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = ((CustomUserDetails) auth.getPrincipal()).getUser();
        
        reviewService.addToFavorites(reviewId, user);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{reviewId}/favorite")
    public ResponseEntity<Void> removeFromFavorites(@PathVariable UUID reviewId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = ((CustomUserDetails) auth.getPrincipal()).getUser();
        
        reviewService.removeFromFavorites(reviewId, user);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/favorites")
    public ResponseEntity<Page<ReviewResponseDto>> getFavoriteReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = ((CustomUserDetails) auth.getPrincipal()).getUser();
        
        return ResponseEntity.ok(reviewService.getFavoriteReviews(user, page, size));
    }
    
    @PostMapping("/{reviewId}/comments")
    public ResponseEntity<ReviewCommentDto> addComment(
            @PathVariable UUID reviewId,
            @RequestBody ReviewCommentRequestDto request) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = ((CustomUserDetails) auth.getPrincipal()).getUser();
        
        return ResponseEntity.ok(reviewService.addComment(reviewId, request, user));
    }
    
    @GetMapping("/{reviewId}/comments")
    public ResponseEntity<Page<ReviewCommentDto>> getCommentsByReview(
            @PathVariable UUID reviewId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reviewService.getCommentsByReview(reviewId, page, size));
    }
}