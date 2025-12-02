package com.ratingapp.movie.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.ratingapp.movie.entity.Actor;
import com.ratingapp.movie.entity.Movie;
import com.ratingapp.movie.repository.MovieRepository;
import com.ratingapp.movie.repository.ActorRepository;

import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class MovieService {
    
    private final MovieRepository movieRepository;
    private final ActorRepository actorRepository;
    private final TmdbApiService tmdbApiService;
    
    public MovieService(MovieRepository movieRepository, 
                       ActorRepository actorRepository,
                       TmdbApiService tmdbApiService) {
        this.movieRepository = movieRepository;
        this.actorRepository = actorRepository;
        this.tmdbApiService = tmdbApiService;
    }
    
    public List<Movie> getPopularMovies(int page) {
        List<Movie> movies = movieRepository.findByOrderByVoteAverageDesc(PageRequest.of(page - 1, 20));
        
        if (movies.isEmpty()) {
            movies = tmdbApiService.getPopularMovies(page);
            movieRepository.saveAll(movies);
        }
        
        return movies;
    }

    public List<Movie> getTrendingMovies(int page) {
        List<Movie> movies = tmdbApiService.getTrendingMovies(page);
        return saveMoviesIfNeeded(movies);
    }

    public List<Movie> getNewReleases(int page) {
        List<Movie> movies = tmdbApiService.getNewReleases(page);
        return saveMoviesIfNeeded(movies);
    }

    public List<Movie> getRecommendedMovies(int page) {
        List<Movie> popularMovies = getPopularMovies(1);
        if (!popularMovies.isEmpty()) {
            Long popularMovieId = popularMovies.get(0).getTmdbId();
            List<Movie> recommendations = tmdbApiService.getRecommendedMovies(popularMovieId, page);
            return saveMoviesIfNeeded(recommendations);
        }
        return Collections.emptyList();
    }
    
    public List<Movie> searchMovies(String query, int page) {
        List<Movie> movies = tmdbApiService.searchMovies(query, page);
        return saveMoviesIfNeeded(movies);
    }
    
    public MovieDetails getMovieDetails(Long tmdbId) {
        Optional<Movie> existingMovie = movieRepository.findByTmdbId(tmdbId);
        if (existingMovie.isPresent()) {
            Movie movie = existingMovie.get();
            List<Actor> actors = actorRepository.findByMovieIdOrderByOrderAsc(movie.getId());
            return new MovieDetails(movie, actors);
        }
        
        TmdbApiService.MovieDetails tmdbDetails = tmdbApiService.getMovieDetails(tmdbId);
        if (tmdbDetails != null) {
            Movie savedMovie = movieRepository.save(tmdbDetails.getMovie());
            List<Actor> savedActors = actorRepository.saveAll(tmdbDetails.getActors());
            
            return new MovieDetails(savedMovie, savedActors);
        }
        
        throw new RuntimeException("Movie not found with tmdbId: " + tmdbId);
    }
    
    @Data
    public static class MovieDetails {
        private Movie movie;
        private List<Actor> actors;
        
        public MovieDetails(Movie movie, List<Actor> actors) {
            this.movie = movie;
            this.actors = actors;
        }
    }
    
    private List<Movie> saveMoviesIfNeeded(List<Movie> movies) {
        return movies.stream()
            .map(movie -> {
                Optional<Movie> existing = movieRepository.findByTmdbId(movie.getTmdbId());
                return existing.orElseGet(() -> movieRepository.save(movie));
            })
            .collect(Collectors.toList());
    }
}