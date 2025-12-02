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
            movies = saveMoviesIfNeeded(movies);
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
    
    @Transactional
    public MovieDetails getMovieDetails(Long tmdbId) {
        // Всегда получаем свежие данные из TMDB
        TmdbApiService.MovieDetails tmdbDetails = tmdbApiService.getMovieDetails(tmdbId);
        
        if (tmdbDetails == null) {
            throw new RuntimeException("Movie not found in TMDB with id: " + tmdbId);
        }
        
        Optional<Movie> existingMovie = movieRepository.findByTmdbId(tmdbId);
        Movie movieToProcess;
        List<Actor> actors;
        
        if (existingMovie.isPresent()) {
            // Обновляем существующий фильм
            Movie existing = existingMovie.get();
            updateMovieFromTmdb(existing, tmdbDetails.getMovie());
            
            // Удаляем старых актеров
            actorRepository.deleteByMovieId(existing.getId());
            
            // Сохраняем обновленный фильм
            movieToProcess = movieRepository.save(existing);
        } else {
            // Сохраняем новый фильм
            movieToProcess = movieRepository.save(tmdbDetails.getMovie());
        }
        
        // Привязываем актеров к фильму
        Movie finalMovie = movieToProcess; // final переменная для лямбды
        tmdbDetails.getActors().forEach(actor -> actor.setMovie(finalMovie));
        
        // Сохраняем актеров
        actors = actorRepository.saveAll(tmdbDetails.getActors());
        
        log.info("Saved/Updated movie: {}, with {} actors", 
                 movieToProcess.getTitle(), actors.size());
        log.debug("Movie details - Budget: {}, Revenue: {}, Runtime: {}, Director: {}, Genres: {}", 
                 movieToProcess.getBudget(), movieToProcess.getRevenue(), 
                 movieToProcess.getRuntime(), movieToProcess.getDirector(),
                 movieToProcess.getGenres());
        
        return new MovieDetails(movieToProcess, actors);
    }
    
    private void updateMovieFromTmdb(Movie existing, Movie newData) {
        existing.setTitle(newData.getTitle());
        existing.setOriginalTitle(newData.getOriginalTitle());
        existing.setOverview(newData.getOverview());
        existing.setPopularity(newData.getPopularity());
        existing.setVoteAverage(newData.getVoteAverage());
        existing.setVoteCount(newData.getVoteCount());
        existing.setReleaseDate(newData.getReleaseDate());
        existing.setRuntime(newData.getRuntime());
        existing.setPosterPath(newData.getPosterPath());
        existing.setBackdropPath(newData.getBackdropPath());
        existing.setStatus(newData.getStatus());
        existing.setOriginalLanguage(newData.getOriginalLanguage());
        existing.setGenres(newData.getGenres());
        existing.setBudget(newData.getBudget());
        existing.setRevenue(newData.getRevenue());
        existing.setDirector(newData.getDirector());
        existing.setScreenwriter(newData.getScreenwriter());
        existing.setProducer(newData.getProducer());
        existing.setComposer(newData.getComposer());
        existing.setCinematographer(newData.getCinematographer());
        existing.setEditor(newData.getEditor());
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
            .map(movieItem -> {
                Optional<Movie> existing = movieRepository.findByTmdbId(movieItem.getTmdbId());
                return existing.orElseGet(() -> movieRepository.save(movieItem));
            })
            .collect(Collectors.toList());
    }
}