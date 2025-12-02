package com.ratingapp.movie.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TmdbMovieResponse {
    
    private Long id;
    private String title;
    
    @JsonProperty("original_title")
    private String originalTitle;
    
    private String overview;
    private Double popularity;
    
    @JsonProperty("vote_average")
    private Double voteAverage;
    
    @JsonProperty("vote_count")
    private Integer voteCount;
    
    @JsonProperty("release_date")
    private String releaseDate;
    
    private Integer runtime;
    
    @JsonProperty("poster_path")
    private String posterPath;
    
    @JsonProperty("backdrop_path")
    private String backdropPath;
    
    private List<Genre> genres;
    private String status;
    
    @JsonProperty("original_language")
    private String originalLanguage;
    
    private Long budget;
    private Long revenue;
    private Credits credits;

    @Data
    public static class Genre {
        private Long id;
        private String name;
    }

    @Data
    public static class Credits {
        private List<Crew> crew;
        private List<Cast> cast;
    }

    @Data
    public static class Crew {
        private Long id;
        private String name;
        private String job;
        private String department;
    }

    @Data
    public static class Cast {
        private Long id;
        private String name;
        private String character;
        private Integer order;
        
        @JsonProperty("profile_path")
        private String profilePath;
    }
}