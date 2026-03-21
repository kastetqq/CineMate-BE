package com.ratingapp.reviews.controller;

import com.ratingapp.reviews.dto.*;
import com.ratingapp.reviews.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(
            @RequestBody ReviewRequestDto request,
            @RequestAttribute("userId") UUID userId) {
        return ResponseEntity.ok(reviewService.createReview(request, userId));
    }
    
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @PathVariable UUID reviewId,
            @RequestBody ReviewRequestDto request,
            @RequestAttribute("userId") UUID userId) {
        return ResponseEntity.ok(reviewService.updateReview(reviewId, request, userId));
    }
    
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID reviewId,
            @RequestAttribute("userId") UUID userId) {
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping
    public ResponseEntity<Page<ReviewResponseDto>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestAttribute(value = "userId", required = false) UUID userId) {
        return ResponseEntity.ok(reviewService.getAllReviews(page, size, userId));
    }
    
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<Page<ReviewResponseDto>> getReviewsByMovie(
            @PathVariable Long movieId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestAttribute(value = "userId", required = false) UUID userId) {
        return ResponseEntity.ok(reviewService.getReviewsByMovie(movieId, page, size, userId));
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ReviewResponseDto>> getReviewsByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestAttribute(value = "userId", required = false) UUID currentUserId) {
        return ResponseEntity.ok(reviewService.getReviewsByUser(userId, page, size, currentUserId));
    }
    
    @GetMapping("/search/movie")
    public ResponseEntity<Page<ReviewResponseDto>> searchByMovieTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestAttribute(value = "userId", required = false) UUID userId) {
        return ResponseEntity.ok(reviewService.searchByMovieTitle(title, page, size, userId));
    }
    
    @GetMapping("/search/user")
    public ResponseEntity<Page<ReviewResponseDto>> searchByUserName(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestAttribute(value = "userId", required = false) UUID userId) {
        return ResponseEntity.ok(reviewService.searchByUserName(username, page, size, userId));
    }
    
    @PostMapping("/{reviewId}/like")
    public ResponseEntity<Void> likeReview(
            @PathVariable UUID reviewId,
            @RequestAttribute("userId") UUID userId) {
        reviewService.likeReview(reviewId, userId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{reviewId}/favorite")
    public ResponseEntity<Void> addToFavorites(
            @PathVariable UUID reviewId,
            @RequestAttribute("userId") UUID userId) {
        reviewService.addToFavorites(reviewId, userId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{reviewId}/favorite")
    public ResponseEntity<Void> removeFromFavorites(
            @PathVariable UUID reviewId,
            @RequestAttribute("userId") UUID userId) {
        reviewService.removeFromFavorites(reviewId, userId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/favorites")
    public ResponseEntity<Page<ReviewResponseDto>> getFavoriteReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestAttribute("userId") UUID userId) {
        return ResponseEntity.ok(reviewService.getFavoriteReviews(userId, page, size, userId));
    }
    
    @PostMapping("/{reviewId}/comments")
    public ResponseEntity<ReviewCommentDto> addComment(
            @PathVariable UUID reviewId,
            @RequestBody ReviewCommentRequestDto request,
            @RequestAttribute("userId") UUID userId) {
        return ResponseEntity.ok(reviewService.addComment(reviewId, request, userId));
    }
    
    @GetMapping("/{reviewId}/comments")
    public ResponseEntity<Page<ReviewCommentDto>> getCommentsByReview(
            @PathVariable UUID reviewId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reviewService.getCommentsByReview(reviewId, page, size));
    }
}