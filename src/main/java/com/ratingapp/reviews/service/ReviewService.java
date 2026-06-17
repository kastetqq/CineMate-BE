package com.ratingapp.reviews.service;

import com.ratingapp.auth.entity.User;
import com.ratingapp.auth.repository.UserRepository;
import com.ratingapp.reviews.dto.*;
import com.ratingapp.reviews.entity.*;
import com.ratingapp.reviews.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository commentRepository;
    private final ReviewReactionRepository reactionRepository;
    private final ReviewFavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    
    @Transactional
public ReviewResponseDto createReview(ReviewRequestDto request, UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));
    
    if (reviewRepository.findByUserIdAndMovieId(userId, request.getMovieId()).isPresent()) {
        throw new RuntimeException("You have already reviewed this movie");
    }
    
    Review review = new Review();
    review.setUser(user);
    review.setMovieId(request.getMovieId());
    review.setMovieTitle(request.getMovieTitle());
    review.setMoviePosterPath(request.getMoviePosterPath());
    review.setMovieReleaseDate(request.getMovieReleaseDate());
    review.setContent(request.getContent());
    review.setRating(request.getRating());
    review.setIsSpoiler(request.getIsSpoiler());
    
    Review saved = reviewRepository.save(review);
    
    return convertToResponseDto(saved, userId);
}
    
@Transactional
public ReviewResponseDto updateReview(UUID reviewId, ReviewRequestDto request, UUID userId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new RuntimeException("Review not found"));
    
    if (!review.getUser().getId().equals(userId)) {
        throw new RuntimeException("You can only update your own reviews");
    }
    
    review.setContent(request.getContent());
    review.setRating(request.getRating());
    review.setIsSpoiler(request.getIsSpoiler());
    
    Review saved = reviewRepository.save(review);
    return convertToResponseDto(saved, userId);
}

@Transactional
public void deleteReview(UUID reviewId, UUID userId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new RuntimeException("Review not found"));
    
    if (!review.getUser().getId().equals(userId)) {
        throw new RuntimeException("You can only delete your own reviews");
    }
    
    reviewRepository.delete(review);
}
    
    public Page<ReviewResponseDto> getAllReviews(int page, int size, UUID currentUserId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.findAll(pageable)
            .map(review -> convertToResponseDto(review, currentUserId));
    }
    
    public Page<ReviewResponseDto> getReviewsByMovie(Long movieId, int page, int size, UUID currentUserId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.findByMovieIdOrderByCreatedAtDesc(movieId, pageable)
            .map(review -> convertToResponseDto(review, currentUserId));
    }
    
    public Page<ReviewResponseDto> getReviewsByUser(UUID userId, int page, int size, UUID currentUserId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map(review -> convertToResponseDto(review, currentUserId));
    }
    
    public Page<ReviewResponseDto> searchByMovieTitle(String movieTitle, int page, int size, UUID currentUserId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.findByMovieTitleContainingIgnoreCaseOrderByCreatedAtDesc(movieTitle, pageable)
            .map(review -> convertToResponseDto(review, currentUserId));
    }
    
    public Page<ReviewResponseDto> searchByUserName(String username, int page, int size, UUID currentUserId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.findByUserNameContaining(username, pageable)
            .map(review -> convertToResponseDto(review, currentUserId));
    }
    
    @Transactional
    public void likeReview(UUID reviewId, UUID userId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found"));
        
        if (reactionRepository.findByUserIdAndReviewId(userId, reviewId).isPresent()) {
            reactionRepository.deleteByUserIdAndReviewId(userId, reviewId);
            review.setLikesCount(review.getLikesCount() - 1);
        } else {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            ReviewReaction reaction = new ReviewReaction();
            reaction.setUser(user);
            reaction.setReview(review);
            reactionRepository.save(reaction);
            review.setLikesCount(review.getLikesCount() + 1);
        }
        
        reviewRepository.save(review);
    }
    
    @Transactional
    public void addToFavorites(UUID reviewId, UUID userId) {
        if (favoriteRepository.existsByUserIdAndReviewId(userId, reviewId)) {
            return;
        }
        
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        ReviewFavorite favorite = new ReviewFavorite();
        favorite.setUser(user);
        favorite.setReview(review);
        favoriteRepository.save(favorite);
    }
    
    @Transactional
    public void removeFromFavorites(UUID reviewId, UUID userId) {
        favoriteRepository.findByUserIdAndReviewId(userId, reviewId)
            .ifPresent(favoriteRepository::delete);
    }
    
    public Page<ReviewResponseDto> getFavoriteReviews(UUID userId, int page, int size, UUID currentUserId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "review.createdAt"));
        return favoriteRepository.findByUserIdOrderByReviewCreatedAtDesc(userId, pageable)
            .map(favorite -> convertToResponseDto(favorite.getReview(), currentUserId));
    }
    
    @Transactional
    public ReviewCommentDto addComment(UUID reviewId, ReviewCommentRequestDto request, UUID userId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        ReviewComment comment = new ReviewComment();
        comment.setReview(review);
        comment.setUser(user);
        comment.setContent(request.getContent());
        
        if (request.getParentId() != null) {
            ReviewComment parent = commentRepository.findById(request.getParentId())
                .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParent(parent);
        }
        
        ReviewComment saved = commentRepository.save(comment);
        
        review.setCommentsCount(review.getCommentsCount() + 1);
        reviewRepository.save(review);
        
        return convertToCommentDto(saved);
    }
    
    public Page<ReviewCommentDto> getCommentsByReview(UUID reviewId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return commentRepository.findByReviewIdAndParentIsNullOrderByCreatedAtDesc(reviewId, pageable)
            .map(this::convertToCommentDto);
    }
    
    private ReviewResponseDto convertToResponseDto(Review review, UUID currentUserId) {
        ReviewResponseDto dto = new ReviewResponseDto();
        dto.setId(review.getId());
        dto.setUserId(review.getUser().getId());
        dto.setUserName(review.getUser().getUsername());
        dto.setMovieId(review.getMovieId());
        dto.setMovieTitle(review.getMovieTitle());
        dto.setMoviePosterPath(review.getMoviePosterPath());
        dto.setMovieReleaseDate(review.getMovieReleaseDate());
        dto.setContent(review.getContent());
        dto.setRating(review.getRating());
        dto.setLikesCount(review.getLikesCount());
        dto.setCommentsCount(review.getCommentsCount());
        dto.setIsSpoiler(review.getIsSpoiler());
        dto.setCreatedAt(review.getCreatedAt());
        
        if (currentUserId != null) {
            dto.setIsLikedByCurrentUser(reactionRepository.existsByUserIdAndReviewId(currentUserId, review.getId()));
            dto.setIsFavoritedByCurrentUser(favoriteRepository.existsByUserIdAndReviewId(currentUserId, review.getId()));
        }
        
        return dto;
    }
    
    private ReviewCommentDto convertToCommentDto(ReviewComment comment) {
        ReviewCommentDto dto = new ReviewCommentDto();
        dto.setId(comment.getId());
        dto.setUserId(comment.getUser().getId());
        dto.setUserName(comment.getUser().getUsername());
        dto.setContent(comment.getContent());
        dto.setLikesCount(comment.getLikesCount());
        dto.setCreatedAt(comment.getCreatedAt());
        
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            dto.setReplies(comment.getReplies().stream()
                .map(this::convertToCommentDto)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
}