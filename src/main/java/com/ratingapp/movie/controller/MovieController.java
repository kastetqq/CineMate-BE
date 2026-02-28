package com.ratingapp.movie.controller;

import com.ratingapp.movie.dto.MovieDetailsResponseDto;
import com.ratingapp.movie.dto.MovieResponseDto;
import com.ratingapp.movie.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Slf4j
public class MovieController {
    
    private final MovieService movieService;
    
    @GetMapping("/trending")
    public ResponseEntity<List<MovieResponseDto>> getTrendingMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        List<MovieResponseDto> movies = movieService.getTrendingMovies(page, size);
        return ResponseEntity.ok(movies);
    }
    
    @GetMapping("/new-releases")
    public ResponseEntity<List<MovieResponseDto>> getNewReleases(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        List<MovieResponseDto> movies = movieService.getNewReleases(page, size);
        return ResponseEntity.ok(movies);
    }
    
    @GetMapping("/top-rated")
    public ResponseEntity<List<MovieResponseDto>> getTopRatedMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        List<MovieResponseDto> movies = movieService.getTopRatedMovies(page, size);
        return ResponseEntity.ok(movies);
    }
    
    @GetMapping("/upcoming")
    public ResponseEntity<List<MovieResponseDto>> getUpcomingMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        List<MovieResponseDto> movies = movieService.getUpcomingMovies(page, size);
        return ResponseEntity.ok(movies);
    }
    
    @GetMapping("/{tmdbId}")
    public ResponseEntity<MovieDetailsResponseDto> getMovieDetails(
            @PathVariable Long tmdbId) {
        
        try {
            MovieDetailsResponseDto details = movieService.getMovieDetails(tmdbId);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            log.error("Error getting movie details for id: {}", tmdbId, e);
            return ResponseEntity.notFound().build();
        }
    }
}