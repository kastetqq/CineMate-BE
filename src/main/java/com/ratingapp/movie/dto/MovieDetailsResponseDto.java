package com.ratingapp.movie.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class MovieDetailsResponseDto {
    private Long tmdbId;
    private String title;
    private String overview;
    private Double voteAverage;
    private Double popularity;
    private Integer runtime;
    private LocalDate releaseDate;

    private String posterUrl;
    private String backdropUrl;
    
    private List<String> genres;

    private String budget;
    private String revenue;
    private List<String> countries;
    private String language;

    private List<ActorDto> cast; 
    private List<CrewDto> directors;
    private List<CrewDto> writers;
    private List<CrewDto> producers;
    private List<CrewDto> composers;
    private List<CrewDto> cinematographers;
    private List<CrewDto> editors;  
}