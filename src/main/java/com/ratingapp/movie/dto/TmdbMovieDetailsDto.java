package com.ratingapp.movie.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class TmdbMovieDetailsDto {

    private Long id;
    private String title;
    private String overview;
    
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

    private Long budget;
    private Long revenue;
    
    private List<GenreDto> genres;
    
    @JsonProperty("production_countries")
    private List<ProductionCountryDto> productionCountries;
    
    @JsonProperty("spoken_languages")
    private List<SpokenLanguageDto> spokenLanguages;
    
    private Credits credits;
    
    @Data
    public static class GenreDto {
        private String name;
    }
    
    @Data
    public static class ProductionCountryDto {
        private String name;
    }
    
    @Data
    public static class SpokenLanguageDto {
        private String name;    
    }
    
    @Data
    public static class Credits {
        private List<CastDto> cast;
        private List<CrewDto> crew;
    }
    
    @Data
    public static class CastDto {
        private String name;
        private String character;
        private Integer order;
        
        @JsonProperty("profile_path")
        private String profilePath;
    }
    
    @Data
    public static class CrewDto {
        private String name;
        private String job;
        private String department;
        
        @JsonProperty("profile_path")
        private String profilePath;
    }

}