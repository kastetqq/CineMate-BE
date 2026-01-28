package com.ratingapp.movie.service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ratingapp.movie.dto.TmdbMovieResponse;
import com.ratingapp.movie.dto.TmdbMovieListResponse;
import com.ratingapp.movie.entity.Actor;
import com.ratingapp.movie.entity.Movie;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TmdbApiService {
    
    @Value("${tmdb.api.key}")
    private String apiKey ;
    
    @Value("${tmdb.api.base-url:https://api.themoviedb.org/3}")
    private String baseUrl;
    
    @Value("${tmdb.image.base-url:https://image.tmdb.org/t/p}")
    private String imageBaseUrl;
    
    private final RestTemplate restTemplate;
    
    public TmdbApiService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
        log.info("TMDB API Service initialized with Bearer Token authentication");
    }

    public List<Movie> getPopularMovies(int page) {
        String url = baseUrl + "/movie/popular?language=en-US&page=" + page;
        return fetchMoviesFromTmdb(url);
    }

    public List<Movie> getTrendingMovies(int page) {
        String url = baseUrl + "/trending/movie/week?language=en-US&page=" + page;
        return fetchMoviesFromTmdb(url);
    }

    public List<Movie> getNewReleases(int page) {
        LocalDate now = LocalDate.now();
        LocalDate threeMonthsAgo = now.minusMonths(3);
        
        String url = baseUrl + "/discover/movie?" + 
                    "language=en-US&page=" + page + 
                    "&sort_by=release_date.desc" +
                    "&release_date.gte=" + threeMonthsAgo +
                    "&release_date.lte=" + now;
        
        return fetchMoviesFromTmdb(url);
    }

    public List<Movie> getRecommendedMovies(Long movieId, int page) {
        String url = baseUrl + "/movie/" + movieId + "/recommendations?language=en-US&page=" + page;
        return fetchMoviesFromTmdb(url);
    }
    
    public List<Movie> searchMovies(String query, int page) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            String url = baseUrl + "/search/movie?query=" + encodedQuery + "&language=en-US&page=" + page;
            
            return fetchMoviesFromTmdb(url);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Encoding error", e);
        }
    }
    
    public MovieDetails getMovieDetails(Long tmdbId) {
        String url = baseUrl + "/movie/" + tmdbId + 
                    "?language=ru&append_to_response=credits,release_dates,videos";
    
        try {
            ResponseEntity<TmdbMovieResponse> response = makeTmdbRequest(url, TmdbMovieResponse.class);
        
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return convertToMovieDetails(response.getBody());
            }
        } catch (Exception e) {
            log.error("Error fetching movie details from TMDB for id: {}", tmdbId, e);
        }
    
        return null;
    }
    
    @Data
    public static class MovieDetails {
        private Movie movie;
        private List<Actor> actors;
        
        public MovieDetails(Movie movie, List<Actor> actors) {
            this.movie = movie;
            this.actors = actors;
        }
    }
    
    private MovieDetails convertToMovieDetails(TmdbMovieResponse tmdbMovie) {
        Movie movie = convertToMovieEntity(tmdbMovie);
        List<Actor> actors = extractActors(tmdbMovie, movie);
        extractCrewMembers(tmdbMovie, movie);
        
        return new MovieDetails(movie, actors);
    }
    
private Movie convertToMovieEntity(TmdbMovieResponse tmdbMovie) {
    Movie movie = new Movie();
    movie.setTmdbId(tmdbMovie.getId());
    movie.setTitle(tmdbMovie.getTitle());
    movie.setOriginalTitle(tmdbMovie.getOriginalTitle());
    movie.setOverview(tmdbMovie.getOverview());
    movie.setPopularity(tmdbMovie.getPopularity());
    movie.setVoteAverage(tmdbMovie.getVoteAverage());
    movie.setVoteCount(tmdbMovie.getVoteCount());
    
    movie.setRuntime(tmdbMovie.getRuntime() != null ? tmdbMovie.getRuntime() : 0);
    
    if (tmdbMovie.getPosterPath() != null) {
        movie.setPosterPath(tmdbMovie.getPosterPath());
    }
    
    if (tmdbMovie.getBackdropPath() != null) {
        movie.setBackdropPath(tmdbMovie.getBackdropPath());
    }
    
    movie.setStatus(tmdbMovie.getStatus() != null ? tmdbMovie.getStatus() : "Unknown");
    movie.setOriginalLanguage(tmdbMovie.getOriginalLanguage() != null ? tmdbMovie.getOriginalLanguage() : "en");
    
    if (tmdbMovie.getReleaseDate() != null && !tmdbMovie.getReleaseDate().isEmpty()) {
        try {
            movie.setReleaseDate(LocalDate.parse(tmdbMovie.getReleaseDate()));
        } catch (DateTimeParseException e) {
            log.warn("Invalid date format: {}", tmdbMovie.getReleaseDate());
            try {
                movie.setReleaseDate(LocalDate.parse(tmdbMovie.getReleaseDate(), 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } catch (DateTimeParseException ex) {
                log.error("Could not parse date: {}", tmdbMovie.getReleaseDate());
            }
        }
    }

    if (tmdbMovie.getGenres() != null && !tmdbMovie.getGenres().isEmpty()) {
        List<String> genreNames = tmdbMovie.getGenres().stream()
            .map(genre -> genre.getName())
            .collect(Collectors.toList());
        movie.setGenres(genreNames);
    } else {
        movie.setGenres(Collections.emptyList());
    }
    
    if (tmdbMovie.getBudget() != null && tmdbMovie.getBudget() > 0) {
        movie.setBudget(BigDecimal.valueOf(tmdbMovie.getBudget()));
    }
    
    if (tmdbMovie.getRevenue() != null && tmdbMovie.getRevenue() > 0) {
        movie.setRevenue(BigDecimal.valueOf(tmdbMovie.getRevenue()));
    }
    
    if (tmdbMovie.getCredits() != null && tmdbMovie.getCredits().getCrew() != null) {
        tmdbMovie.getCredits().getCrew().stream()
        .filter(crew -> "Director".equals(crew.getJob()))
        .findFirst()
        .ifPresent(director -> {
            movie.setDirector(director.getName());
            log.debug("Found director for movie {}: {}", movie.getTitle(), director.getName());
        });
    }
    
    return movie;
}
    
    private List<Actor> extractActors(TmdbMovieResponse tmdbMovie, Movie movie) {
        if (tmdbMovie.getCredits() == null || tmdbMovie.getCredits().getCast() == null) {
            return Collections.emptyList();
        }

        return tmdbMovie.getCredits().getCast().stream()
            .limit(10)
            .map(cast -> {
                Actor actor = new Actor();
                actor.setTmdbId(cast.getId());
                actor.setName(cast.getName());
                actor.setCharacter(cast.getCharacter());
                actor.setProfilePath(cast.getProfilePath());
                actor.setOrder(cast.getOrder());
                actor.setMovie(movie);
                return actor;
            })
            .collect(Collectors.toList());
    }
    
private void extractCrewMembers(TmdbMovieResponse tmdbMovie, Movie movie) {
    Map<String, List<TmdbMovieResponse.Crew>> crewByJob = tmdbMovie.getCredits().getCrew().stream()
        .collect(Collectors.groupingBy(TmdbMovieResponse.Crew::getJob));

    extractMainCrew(crewByJob, "Director", movie::setDirector);
    extractMainCrew(crewByJob, "Screenplay", movie::setScreenwriter);
    extractMainCrew(crewByJob, "Writer", movie::setScreenwriter);
    extractMainCrew(crewByJob, "Producer", movie::setProducer);
    extractMainCrew(crewByJob, "Original Music Composer", movie::setComposer);
    extractMainCrew(crewByJob, "Director of Photography", movie::setCinematographer);
    extractMainCrew(crewByJob, "Editor", movie::setEditor);

    if (movie.getDirector() == null) {
        extractMainCrew(crewByJob, "Series Director", movie::setDirector);
    }
}

private void extractMainCrew(Map<String, List<TmdbMovieResponse.Crew>> crewByJob, 
                           String job, Consumer<String> setter) {
    List<TmdbMovieResponse.Crew> crewList = crewByJob.get(job);
    if (crewList != null && !crewList.isEmpty()) {
        setter.accept(crewList.get(0).getName());
        
        log.info("Found {}: {}", job, crewList.get(0).getName());
    }
}

private List<Movie> fetchMoviesFromTmdb(String url) {
    log.info("Starting to fetch movies from TMDB: {}", url);
    
    try {
        ResponseEntity<TmdbMovieListResponse> response = makeTmdbRequest(url, TmdbMovieListResponse.class);
        
        log.info("Response status: {}", response.getStatusCode());
        
        if (response.getStatusCode().is2xxSuccessful()) {
            TmdbMovieListResponse body = response.getBody();
            
            if (body == null) {
                log.warn("TMDB API returned null body");
                return Collections.emptyList();
            }
            
            log.info("TMDB Response - Page: {}, Total Pages: {}, Total Results: {}", 
                body.getPage(), body.getTotalPages(), body.getTotalResults());
            
            if (body.getResults() != null) {
                log.info("Received {} movies from TMDB", body.getResults().size());
                
                if (!body.getResults().isEmpty()) {
                    List<Movie> movies = body.getResults().stream()
                        .map(this::convertToMovieEntity)
                        .collect(Collectors.toList());
                    
                    log.info("Successfully converted {} movies", movies.size());
                    return movies;
                } else {
                    log.warn("TMDB API returned empty results array");
                }
            } else {
                log.warn("TMDB API returned null results");
            }
        } else {
            log.error("TMDB API returned non-2xx status: {}", response.getStatusCode());
        }
        
    } catch (Exception e) {
        log.error("Critical error fetching movies from TMDB for URL: {}", url, e);
        throw new RuntimeException("Failed to fetch movies from TMDB: " + e.getMessage(), e);
    }
    
    return Collections.emptyList();
}
    
private <T> ResponseEntity<T> makeTmdbRequest(String url, Class<T> responseType) {
    try {
        HttpHeaders headers = new HttpHeaders();
        headers.set("accept", "application/json");
        headers.set("Authorization", "Bearer " + apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        log.info("Calling TMDB API: {}", url);
        log.info("Headers: {}", headers);
        
        restTemplate.getInterceptors().add((request, body, execution) -> {
            log.info("Request URI: {}", request.getURI());
            log.info("Request Headers: {}", request.getHeaders());
            return execution.execute(request, body);
        });
        
        ResponseEntity<T> response = this.restTemplate.exchange(
            url, 
            HttpMethod.GET, 
            entity, 
            responseType
        );
        
        log.info("TMDB API Response Status: {}", response.getStatusCode());
        log.info("TMDB API Response Headers: {}", response.getHeaders());
        
        if (response.getBody() != null) {
            log.info("TMDB API Response Body type: {}", response.getBody().getClass().getSimpleName());
        }
        
        return response;
        
    } catch (Exception e) {
        log.error("Exception while calling TMDB API: {}", url, e);
        log.error("Exception type: {}", e.getClass().getName());
        log.error("Exception message: {}", e.getMessage());
        
        throw new RuntimeException("TMDB API request failed: " + e.getMessage(), e);
    }
}
    
    public String getImageBaseUrl() {
        return imageBaseUrl;
    }
    
    public String getApiKey() {
        return apiKey;
    }
}