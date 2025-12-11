package com.ratingapp.watchlist.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ratingapp.auth.entity.User;
import com.ratingapp.movie.entity.Movie;
import com.ratingapp.movie.repository.MovieRepository;
import com.ratingapp.movie.service.TmdbApiService;
import com.ratingapp.watchlist.dto.MovieDto;
import com.ratingapp.watchlist.entity.UserMovieStatus;
import com.ratingapp.watchlist.enums.MovieStatus;
import com.ratingapp.watchlist.repository.UserMovieStatusRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovieStatusService {

    private final MovieRepository movieRepository;
    private final UserMovieStatusRepository statusRepository;
    private final TmdbApiService tmdbService;

    public void setStatus(Long tmdbId, User user, MovieStatus status) {
        Movie movie = movieRepository.findByTmdbId(tmdbId)
            .orElseGet(() -> fetchAndSaveFromTmdb(tmdbId));

        UserMovieStatus ums = statusRepository.findByUserAndMovie(user, movie)
            .orElse(new UserMovieStatus(null, user, movie, status, LocalDateTime.now()));

        ums.setStatus(status);
        ums.setAddedDate(LocalDateTime.now());

        statusRepository.save(ums);
    }

    private Movie fetchAndSaveFromTmdb(Long tmdbId) {
        TmdbApiService.MovieDetails movieDetails = tmdbService.getMovieDetails(tmdbId);
        
        if (movieDetails == null || movieDetails.getMovie() == null) {
            throw new RuntimeException("Movie not found in TMDB with id: " + tmdbId);
        }
        
        Movie movie = movieDetails.getMovie();
        
        movie = movieRepository.save(movie);
        
        return movie;
    }

    public void deleteStatus(Long tmdbId, User user) {
        Movie movie = movieRepository.findByTmdbId(tmdbId)
            .orElseThrow(() -> new RuntimeException("Movie not found"));

        statusRepository.findByUserAndMovie(user, movie)
            .ifPresent(statusRepository::delete);
    }

public Map<MovieStatus, List<MovieDto>> getUserLists(User user) {
    return statusRepository.findAllByUser(user).stream()
        .collect(Collectors.groupingBy(
            UserMovieStatus::getStatus,
            Collectors.mapping(ums -> new MovieDto(
                    ums.getMovie().getId(),
                    ums.getMovie().getTitle(),
                    ums.getMovie().getPosterPath(),
                    ums.getMovie().getReleaseDate(),
                    ums.getStatus()
            ), Collectors.toList())
        ));
}
    
public List<MovieDto> getMoviesByStatus(User user, MovieStatus status) {
    return statusRepository.findAllByUserAndStatus(user, status).stream()
    .map(ums -> new MovieDto(
        ums.getMovie().getId(),
        ums.getMovie().getTitle(),
        ums.getMovie().getPosterPath(),
        ums.getMovie().getReleaseDate(),
        ums.getStatus()
    ))
    .toList();
}
}

