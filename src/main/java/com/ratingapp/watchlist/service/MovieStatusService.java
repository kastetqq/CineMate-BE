package com.ratingapp.watchlist.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ratingapp.auth.entity.User;
import com.ratingapp.movie.dto.TmdbMovieDetailsDto;
import com.ratingapp.movie.service.TmdbApiService;
import com.ratingapp.watchlist.dto.MovieDto;
import com.ratingapp.watchlist.entity.UserMovieStatus;
import com.ratingapp.watchlist.enums.MovieStatus;
import com.ratingapp.watchlist.repository.UserMovieStatusRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieStatusService {

    private final UserMovieStatusRepository statusRepository;
    private final TmdbApiService tmdbService;

    @Transactional
    public void setStatus(Long tmdbId, User user, MovieStatus status) {
        UserMovieStatus ums = statusRepository.findByUserAndTmdbId(user, tmdbId)
            .orElseGet(() -> createFromTmdb(tmdbId, user));

        ums.setStatus(status);
        ums.setAddedDate(LocalDateTime.now());

        statusRepository.save(ums);
        
        log.info("User {} set movie {} status to {}", user.getId(), tmdbId, status);
    }

    private UserMovieStatus createFromTmdb(Long tmdbId, User user) {
        TmdbMovieDetailsDto movieDetails = tmdbService.getMovieDetails(tmdbId);
        
        if (movieDetails == null) {
            throw new RuntimeException("Movie not found in TMDB with id: " + tmdbId);
        }
        
        UserMovieStatus ums = new UserMovieStatus();
        ums.setUser(user);
        ums.setTmdbId(tmdbId);
        ums.setTitle(movieDetails.getTitle());
        ums.setVoteAverage(movieDetails.getVoteAverage());
        
        if (movieDetails.getReleaseDate() != null && !movieDetails.getReleaseDate().isEmpty()) {
            ums.setReleaseDate(LocalDate.parse(movieDetails.getReleaseDate()));
        }
        
        ums.setPosterPath(movieDetails.getPosterPath());
        
        return ums;
    }

    @Transactional
    public void deleteStatus(Long tmdbId, User user) {
        statusRepository.findByUserAndTmdbId(user, tmdbId)
            .ifPresent(statusRepository::delete);
    }

    public Map<MovieStatus, List<MovieDto>> getUserLists(User user) {
        return statusRepository.findAllByUser(user).stream()
            .collect(Collectors.groupingBy(
                UserMovieStatus::getStatus,
                Collectors.mapping(this::convertToDto, Collectors.toList())
            ));
    }
    
    public List<MovieDto> getMoviesByStatus(User user, MovieStatus status) {
        return statusRepository.findAllByUserAndStatus(user, status).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    private MovieDto convertToDto(UserMovieStatus ums) {
        MovieDto dto = new MovieDto();
        dto.setTmdbId(ums.getTmdbId());
        dto.setTitle(ums.getTitle());
        dto.setVoteAverage(ums.getVoteAverage());
        dto.setReleaseDate(ums.getReleaseDate());
        dto.setPosterPath(ums.getPosterPath());
        dto.setStatus(ums.getStatus());
        dto.setAddedDate(ums.getAddedDate());
        return dto;
    }
}