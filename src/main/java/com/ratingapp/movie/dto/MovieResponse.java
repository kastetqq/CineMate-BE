package com.ratingapp.movie.dto;

import com.ratingapp.movie.entity.Movie;
import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class MovieResponse {
    private String id;
    private Long tmdbId;
    private String title;
    private String originalTitle;
    private String overview;
    private Double popularity;
    private Double voteAverage;
    private Integer voteCount;
    private String releaseDate;
    private Integer runtime;
    private String posterUrl;
    private String backdropUrl;
    private String status;
    private String originalLanguage;
    private java.math.BigDecimal budget;
    private java.math.BigDecimal revenue;
    private String director;
    private String screenwriter;
    private String producer;
    private String composer;
    private String cinematographer;
    private String editor;
    private java.util.List<String> genres;

    public MovieResponse(Movie movie, String imageBaseUrl) {
        this.id = movie.getId() != null ? movie.getId().toString() : null;
        this.tmdbId = movie.getTmdbId();
        this.title = movie.getTitle();
        this.originalTitle = movie.getOriginalTitle();
        this.overview = movie.getOverview();
        this.popularity = movie.getPopularity();
        this.voteAverage = movie.getVoteAverage();
        this.voteCount = movie.getVoteCount();
        this.releaseDate = movie.getReleaseDate() != null ? movie.getReleaseDate().toString() : null;
        this.runtime = movie.getRuntime();
        this.posterUrl = movie.getPosterPath() != null ? imageBaseUrl + "/w500" + movie.getPosterPath() : null;
        this.backdropUrl = movie.getBackdropPath() != null ? imageBaseUrl + "/w1280" + movie.getBackdropPath() : null;
        this.status = movie.getStatus();
        this.originalLanguage = movie.getOriginalLanguage();
        this.budget = movie.getBudget();
        this.revenue = movie.getRevenue();
        this.director = movie.getDirector();
        this.screenwriter = movie.getScreenwriter();
        this.producer = movie.getProducer();
        this.composer = movie.getComposer();
        this.cinematographer = movie.getCinematographer();
        this.editor = movie.getEditor();
        this.genres = movie.getGenres();
    }
}