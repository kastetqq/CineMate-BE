package com.ratingapp.movie.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ActorDto {
    private Long tmdbId;
    private String name;
    private String character;
    private String profilePath;
    private Integer order;
    private String profileUrl;
}