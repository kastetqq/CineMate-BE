package com.ratingapp.movie.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ratingapp.movie.entity.Actor;

@Repository
public interface ActorRepository extends JpaRepository<Actor, UUID> {
    List<Actor> findByMovieIdOrderByOrderAsc(UUID movieId);
    void deleteByMovieId(UUID movieId);
}