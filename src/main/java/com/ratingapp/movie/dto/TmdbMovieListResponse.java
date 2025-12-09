package com.ratingapp.movie.dto;

import lombok.Data;

import java.util.List;

@Data
public class TmdbMovieListResponse {
    private Integer page;
    private List<TmdbMovieResponse> results;
    private Integer totalPages;
    private Integer totalResults;
}