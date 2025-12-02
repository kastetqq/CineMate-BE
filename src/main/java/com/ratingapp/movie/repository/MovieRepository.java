package com.ratingapp.movie.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ratingapp.movie.entity.Movie;

@Repository
public interface MovieRepository extends JpaRepository<Movie, UUID> {
    Optional<Movie> findByTmdbId(Long tmdbId);
    List<Movie> findByOrderByVoteAverageDesc(Pageable pageable);
    List<Movie> findByOrderByReleaseDateDesc(Pageable pageable);
    List<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}