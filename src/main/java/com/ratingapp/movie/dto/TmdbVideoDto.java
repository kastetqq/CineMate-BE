package com.ratingapp.movie.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TmdbVideoDto {
    private String key;
    private String name;
    private String site;
    private String type;
    private Integer size;
    
    @JsonProperty("official")
    private boolean official;
}