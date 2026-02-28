package com.ratingapp.movie.repository;

import com.ratingapp.movie.entity.Movie;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MovieRepository extends JpaRepository<Movie, UUID> {

    Optional<Movie> findByTmdbId(Long tmdbId);

    List<Movie> findByTopRatedTrueOrderByVoteAverageDesc(Pageable pageable);
    List<Movie> findByTrendingTrueOrderByPopularityDesc(Pageable pageable);
    List<Movie> findByNewReleaseTrueOrderByReleaseDateDesc(Pageable pageable);
    List<Movie> findByUpcomingTrueOrderByReleaseDateDesc(Pageable pageable);

    @Modifying
    @Query("UPDATE Movie m SET m.topRated = false")
    void resetTopRatedFlag();

    @Modifying
    @Query("UPDATE Movie m SET m.trending = false")
    void resetTrendingFlag();

    @Modifying
    @Query("UPDATE Movie m SET m.newRelease = false")
    void resetNewReleaseFlag();

    @Modifying
    @Query("UPDATE Movie m SET m.upcoming = false")
    void resetUpcomingFlag();

    @Modifying
    @Query("DELETE FROM Movie m WHERE m.topRated = false AND m.trending = false AND m.newRelease = false AND m.upcoming = false")
    void deleteMoviesNotInAnyCategory();
}