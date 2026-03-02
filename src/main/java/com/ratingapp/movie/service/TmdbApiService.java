package com.ratingapp.movie.service;

import com.ratingapp.movie.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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
            .queryParam("primary_release_date.gte", now)
            .queryParam("primary_release_date.lte", sixMonthsAgo)
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
        LocalDate sixMonthsLater = sixteenDays.plusMonths(6);
        
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/discover/movie")
            .queryParam("language", "ru-RU")
            .queryParam("page", page)
            .queryParam("sort_by", "popularity.desc")
            .queryParam("primary_release_date.gte", sixteenDays)
            .queryParam("primary_release_date.lte", sixMonthsLater)
            .toUriString();
        
        return fetchMovieList(url);
    }
    
    private List<TmdbMovieDto> fetchMovieList(String url) {
        try {
            ResponseEntity<TmdbMovieListResponseDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(),
                TmdbMovieListResponseDto.class
            );
            
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