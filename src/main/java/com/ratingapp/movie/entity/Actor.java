package com.ratingapp.movie.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "actors")
@Data
public class Actor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tmdb_id")
    private Long tmdbId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 500)
    private String character;

    @Column(name = "profile_path", length = 500)
    private String profilePath;

    @Column(name = "\"order\"")
    private Integer order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private Movie movie;
}