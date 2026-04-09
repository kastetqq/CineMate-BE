package com.ratingapp.watchlist.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ratingapp.auth.entity.User;
import com.ratingapp.auth.service.UserService;
import com.ratingapp.watchlist.dto.MovieStatusRequest;
import com.ratingapp.watchlist.enums.MovieStatus;
import com.ratingapp.watchlist.service.MovieStatusService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
@Slf4j
public class MovieStatusController {

    private final MovieStatusService movieStatusService;
    private final UserService userService;

    @PostMapping("/{movieId}/status")
    public ResponseEntity<?> setStatus(
        @PathVariable Long movieId,
        @RequestBody MovieStatusRequest req,
        @RequestAttribute("userId") UUID userId) {
            
        User user = userService.findById(userId);
        movieStatusService.setStatus(movieId, user, req.getStatus());
        
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<?> deleteStatus(
        @PathVariable Long movieId,
        @RequestAttribute("userId") UUID userId) {

        User user = userService.findById(userId);
        movieStatusService.deleteStatus(movieId, user);
        
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    public ResponseEntity<?> getMyLists(
        @RequestParam(required = false) MovieStatus status,
        @RequestAttribute("userId") UUID userId) {
            
        User user = userService.findById(userId);
        
        if (status == null) {
            return ResponseEntity.ok(movieStatusService.getUserLists(user));
        }
        
        return ResponseEntity.ok(movieStatusService.getMoviesByStatus(user, status));
    }
}