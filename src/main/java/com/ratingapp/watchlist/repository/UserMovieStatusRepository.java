package com.ratingapp.watchlist.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ratingapp.auth.entity.User;
import com.ratingapp.watchlist.entity.UserMovieStatus;
import com.ratingapp.watchlist.enums.MovieStatus;

public interface UserMovieStatusRepository extends JpaRepository<UserMovieStatus, UUID> {
    
    Optional<UserMovieStatus> findByUserAndTmdbId(User user, Long tmdbId);
    
    List<UserMovieStatus> findAllByUser(User user);
    
    List<UserMovieStatus> findAllByUserAndStatus(User user, MovieStatus status);
}