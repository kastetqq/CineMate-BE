package com.ratingapp.movie.dto;

import java.time.LocalDate;
import java.util.List;
import com.ratingapp.movie.entity.Movie;
import com.ratingapp.movie.enums.Genre;
import lombok.Data;

@Data
public class MovieResponseDto {
    private String id;
    private Long tmdbId;
    private String title;
    private String overview;
    private Double voteAverage;
    private LocalDate releaseDate;
    private String posterUrl;
    private List<String> genres;
    private Integer runtime;

    public MovieResponseDto(Movie movie, String imageBaseUrl) {
        this.id = movie.getId() != null ? movie.getId().toString() : null;
        this.tmdbId = movie.getTmdbId();
        this.title = movie.getTitle();
        this.overview = movie.getOverview();
        this.voteAverage = movie.getVoteAverage();
        this.releaseDate = movie.getReleaseDate();
        this.posterUrl = movie.getPosterPath() != null ?
            imageBaseUrl + "/w500" + movie.getPosterPath() : null;
        this.genres = movie.getGenreNames();
        this.runtime = movie.getRuntime();
    }
    
    public MovieResponseDto(TmdbMovieDto dto, String imageBaseUrl) {
        this.id = null;
        this.tmdbId = dto.getId();
        this.title = dto.getTitle();
        this.overview = dto.getOverview();
        this.voteAverage = dto.getVoteAverage();
        
        if (dto.getReleaseDate() != null && !dto.getReleaseDate().isEmpty()) {
            this.releaseDate = LocalDate.parse(dto.getReleaseDate());
        }
        
        this.posterUrl = dto.getPosterPath() != null ?
            imageBaseUrl + "/w500" + dto.getPosterPath() : null;
        
        if (dto.getGenreIds() != null) {
            this.genres = Genre.toDisplayNames(dto.getGenreIds());
        }
        
        this.runtime = dto.getRuntime();
    }
}