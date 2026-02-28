package com.ratingapp.movie.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CrewDto {
    private Long tmdbId;
    private String name;
    private String job;
    private String profilePath;
    private String profileUrl;
}