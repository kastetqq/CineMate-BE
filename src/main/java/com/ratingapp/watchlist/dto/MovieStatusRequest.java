package com.ratingapp.watchlist.dto;

import com.ratingapp.watchlist.enums.MovieStatus;

import lombok.Data;

@Data
public class MovieStatusRequest {
    private MovieStatus status;
}

