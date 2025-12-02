package com.ratingapp.movie.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ratingapp.movie.dto.MovieDetailsResponse;
import com.ratingapp.movie.dto.MovieResponse;
import com.ratingapp.movie.entity.Movie;
import com.ratingapp.movie.service.MovieService;
import com.ratingapp.movie.service.TmdbApiService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor

public class MovieController {
    
    private final MovieService movieService;
    private final TmdbApiService tmdbApiService;

    @GetMapping("/popular")
    public ResponseEntity<List<MovieResponse>> getPopularMovies(@RequestParam(defaultValue = "1") int page) {
        List<Movie> movies = movieService.getPopularMovies(page);
        return ResponseEntity.ok(convertToResponse(movies));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieResponse>> searchMovies(
        @RequestParam String query, 
        @RequestParam(defaultValue = "1") int page) {
        List<Movie> movies = movieService.searchMovies(query, page);
        return ResponseEntity.ok(convertToResponse(movies));
    }

    @GetMapping("/new-releases")
    public ResponseEntity<List<MovieResponse>> getNewReleases(
        @RequestParam(defaultValue = "1") int page) {
        List<Movie> movies = movieService.getNewReleases(page);
        return ResponseEntity.ok(convertToResponse(movies));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<MovieResponse>> getRecommendedMovies(
        @RequestParam(defaultValue = "1") int page) {
        List<Movie> movies = movieService.getRecommendedMovies(page);
        return ResponseEntity.ok(convertToResponse(movies));
    }
    
    @GetMapping("/trending")
    public ResponseEntity<List<MovieResponse>> getTrendingMovies(@RequestParam(defaultValue = "1") int page) {
        List<Movie> movies = movieService.getTrendingMovies(page);
        return ResponseEntity.ok(convertToResponse(movies));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MovieDetailsResponse> getMovieDetails(@PathVariable Long id) {
        MovieService.MovieDetails details = movieService.getMovieDetails(id);
        
        MovieDetailsResponse response = new MovieDetailsResponse(
            details.getMovie(), 
            details.getActors(), 
            tmdbApiService.getImageBaseUrl()
        );
        
        return ResponseEntity.ok(response);
    }
    
    private List<MovieResponse> convertToResponse(List<Movie> movies) {
        return movies.stream()
            .map(movie -> new MovieResponse(movie, tmdbApiService.getImageBaseUrl()))
            .collect(Collectors.toList());
    }
}