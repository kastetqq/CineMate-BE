package com.ratingapp.movie.service;

import com.ratingapp.movie.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class TmdbApiService {
    
    @Value("${tmdb.api.key}")
    private String apiKey;
    
    @Value("${tmdb.api.base-url}")
    private String baseUrl;
    
    private final RestTemplate restTemplate;
    
    public TmdbApiService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }
    
    private HttpEntity<String> createHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        return new HttpEntity<>(headers);
    }
    
    public List<TmdbMovieDto> getTrendingMovies(int page) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/trending/movie/week")
            .queryParam("language", "ru-RU")
            .queryParam("page", page)
            .toUriString();
        
        return fetchMovieList(url);
    }
    
    public List<TmdbMovieDto> getNewReleases(int page) {
        LocalDate now = LocalDate.now();
        LocalDate sixMonthsAgo = now.minusMonths(6);
        
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/discover/movie")
            .queryParam("language", "ru-RU")
            .queryParam("page", page)
            .queryParam("sort_by", "popularity.desc")
            .queryParam("primary_release_date.gte", sixMonthsAgo)
            .queryParam("primary_release_date.lte", now)
            .queryParam("vote_count.gte", 50)
            .toUriString();
        
        return fetchMovieList(url);
    }
    
    public List<TmdbMovieDto> getTopRatedMovies(int page) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/movie/top_rated")
            .queryParam("language", "ru-RU")
            .queryParam("page", page)
            .toUriString();
        
        return fetchMovieList(url);
    }
    
    public List<TmdbMovieDto> getUpcomingMovies(int page) {
        LocalDate sixteenDays = LocalDate.now().plusDays(16);
        LocalDate sixMonthsLater = LocalDate.now().plusMonths(6);
        
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/discover/movie")
            .queryParam("language", "ru-RU")
            .queryParam("page", page)
            .queryParam("sort_by", "popularity.desc")
            .queryParam("primary_release_date.gte", sixteenDays)
            .queryParam("primary_release_date.lte", sixMonthsLater)
            .toUriString();
        
        return fetchMovieList(url);
    }
    
    public List<TmdbMovieDto> searchMovies(String query, int page) {
        try {
            log.info("Original query: {}", query);
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            log.info("Encoded query: {}", encodedQuery);
            int safePage = Math.max(page, 1);
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search/movie")
                .queryParam("query", encodedQuery)
                .queryParam("language", "ru-RU")
                .queryParam("page", safePage)
                .toUriString();
            return fetchMovieList(url);
        } catch (Exception e) {
            log.error("Failed to encode search query", e);
            return Collections.emptyList();
        }
    }
    
    private List<TmdbMovieDto> fetchMovieList(String url) {
        try {
            ResponseEntity<TmdbMovieListResponseDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(),
                TmdbMovieListResponseDto.class
            );
            
            log.info("Response status: {}", response.getStatusCode());

            if (response.getBody() != null && response.getBody().getResults() != null) {
                return response.getBody().getResults();
            }
        } catch (Exception e) {
            log.error("Error fetching movies from TMDB: {}", url, e);
        }
        
        return Collections.emptyList();
    }
    
    public TmdbMovieDetailsDto getMovieDetails(Long tmdbId) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/movie/" + tmdbId)
            .queryParam("language", "ru-RU")
            .queryParam("append_to_response", "credits")
            .toUriString();
        
        try {
            ResponseEntity<TmdbMovieDetailsDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(),
                TmdbMovieDetailsDto.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching movie details for id: {}", tmdbId, e);
            return null;
        }
    }
}