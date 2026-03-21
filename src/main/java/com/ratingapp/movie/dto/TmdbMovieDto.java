package com.ratingapp.movie.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class TmdbMovieDto {
    private Long id;
    private String title;
    private String overview;
    
    @JsonProperty("vote_average")
    private Double voteAverage;

    @JsonProperty("runtime")
    private Integer runtime;
    
    @JsonProperty("popularity")
    private Double popularity;
    
    @JsonProperty("release_date")
    private String releaseDate;
    
    @JsonProperty("poster_path")
    private String posterPath;
    
    @JsonProperty("genre_ids")
    private List<Integer> genreIds;
}