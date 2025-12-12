package com.ratingapp.watchlist.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ratingapp.auth.entity.User;
import com.ratingapp.movie.entity.Movie;
import com.ratingapp.watchlist.enums.MovieStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_movie_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMovieStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Movie movie;

    @Enumerated(EnumType.STRING)
    private MovieStatus status;

    private LocalDateTime addedDate = LocalDateTime.now();
}
