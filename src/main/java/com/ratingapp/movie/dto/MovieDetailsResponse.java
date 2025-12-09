package com.ratingapp.movie.dto;

import com.ratingapp.movie.entity.Movie;
import com.ratingapp.movie.entity.Actor;
import lombok.Data;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class MovieDetailsResponse {
    private MovieResponse movie;
    private List<ActorResponse> actors;

    public MovieDetailsResponse(Movie movie, List<Actor> actors, String imageBaseUrl) {
        this.movie = new MovieResponse(movie, imageBaseUrl);
        this.actors = actors.stream()
                .map(actor -> new ActorResponse(actor, imageBaseUrl))
                .collect(Collectors.toList());
    }
}