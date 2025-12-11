package com.ratingapp.watchlist.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ratingapp.auth.entity.User;
import com.ratingapp.movie.entity.Movie;
import com.ratingapp.watchlist.entity.UserMovieStatus;
import com.ratingapp.watchlist.enums.MovieStatus;

public interface UserMovieStatusRepository extends JpaRepository<UserMovieStatus, UUID> {
    Optional<UserMovieStatus> findByUserAndMovie(User user, Movie movie);

    List<UserMovieStatus> findAllByUser(User user);

    List<UserMovieStatus> findAllByUserAndStatus(User user, MovieStatus status);
}

