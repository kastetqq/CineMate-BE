package com.ratingapp.watchlist.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ratingapp.auth.entity.User;
import com.ratingapp.auth.security.CustomUserDetails;
import com.ratingapp.watchlist.dto.MovieStatusRequest;
import com.ratingapp.watchlist.enums.MovieStatus;
import com.ratingapp.watchlist.service.MovieStatusService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
public class MovieStatusController {

    private final MovieStatusService movieStatusService;

    @PostMapping("/{movieId}/status")
    public ResponseEntity<?> setStatus(
            @PathVariable Long movieId,
            @RequestBody MovieStatusRequest req) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = ((CustomUserDetails) auth.getPrincipal()).getUser();
        
        movieStatusService.setStatus(movieId, user, req.getStatus());
        
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<?> deleteStatus(@PathVariable Long movieId) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = ((CustomUserDetails) auth.getPrincipal()).getUser();
        
        movieStatusService.deleteStatus(movieId, user);
        
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    public ResponseEntity<?> getMyLists(@RequestParam(required = false) MovieStatus status) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = ((CustomUserDetails) auth.getPrincipal()).getUser();
        
        if (status == null) {
            return ResponseEntity.ok(movieStatusService.getUserLists(user));
        }
        
        return ResponseEntity.ok(movieStatusService.getMoviesByStatus(user, status));
    }
}