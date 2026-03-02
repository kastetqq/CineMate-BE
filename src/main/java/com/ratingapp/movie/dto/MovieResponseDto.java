package com.ratingapp.movie.dto;

import java.time.LocalDate;
import java.util.List;

import com.ratingapp.movie.entity.Movie;

import lombok.Data;

@Data
public class MovieResponseDto {
    private String id;
    private Long tmdbId;
    private String title;
    private String overview;
    private Double voteAverage;
    private Double popularity;
    private LocalDate releaseDate;
    private String posterUrl;
    private List<String> genres;

    public MovieResponseDto(Movie movie, String imageBaseUrl){
        this.id = movie.getId() != null ? movie.getId().toString() : null;
        this.tmdbId = movie.getTmdbId();
        this.title = movie.getTitle();
        this.overview = movie.getOverview();
        this.voteAverage = movie.getVoteAverage();
        this.popularity = movie.getPopularity();
        this.releaseDate = movie.getReleaseDate();

        this.posterUrl = movie.getPosterPath() != null ?
            imageBaseUrl + "/w500" + movie.getPosterPath() : null;
        
        this.genres = movie.getGenreNames();
    }
}