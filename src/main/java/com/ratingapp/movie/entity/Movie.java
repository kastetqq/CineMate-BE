package com.ratingapp.movie.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.ratingapp.movie.enums.Genre;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "movies", indexes = {
    @Index(name = "idx_tmdb_id", columnList = "tmdbId", unique = true),
    @Index(name = "idx_top_rated", columnList = "topRated"),
    @Index(name = "idx_trending", columnList = "trending"),
    @Index(name = "idx_new_release", columnList = "newRelease"),
    @Index(name = "idx_upcoming", columnList = "upcoming"),
})
@Data
@NoArgsConstructor
public class Movie {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private Long tmdbId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "overview", length = 2000)
    private String overview;

    @Column(name = "vote_average")
    private Double voteAverage;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "runtime")
    private Integer runtime;

    @Column(name = "poster_path", length = 500)
    private String posterPath;

    @Column(columnDefinition = "integer[]")
    private List<Integer> genreIds = new ArrayList<>();

    @Column(name = "top_rated", nullable = false)
    private boolean topRated = false;

    @Column(nullable = false)
    private boolean trending = false;

    @Column(name = "new_release", nullable = false)
    private boolean newRelease = false;

    @Column(nullable = false)
    private boolean upcoming = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public List<String> getGenreNames() {
        return Genre.toDisplayNames(this.genreIds);
    }

    public void setGenresFromList(List<Genre> genres) {
        this.genreIds = Genre.toTmdbIds(genres);
    }
}