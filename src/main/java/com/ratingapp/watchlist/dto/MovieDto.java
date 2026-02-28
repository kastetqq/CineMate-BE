package com.ratingapp.watchlist.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.ratingapp.watchlist.enums.MovieStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieDto {

    private Long tmdbId;
    private String title;
    private Double voteAverage;
    private LocalDate releaseDate;
    private String posterPath;
    private MovieStatus status;
    private LocalDateTime addedDate;
    
}