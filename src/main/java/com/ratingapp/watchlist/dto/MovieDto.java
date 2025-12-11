package com.ratingapp.watchlist.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.ratingapp.watchlist.enums.MovieStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieDto {

    private UUID id;
    private String title;
    private String posterPath;
    private LocalDate releaseDate;
    private MovieStatus status;

    
}
