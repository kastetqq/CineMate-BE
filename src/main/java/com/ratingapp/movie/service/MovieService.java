package com.ratingapp.movie.service;

import com.ratingapp.movie.dto.*;
import com.ratingapp.movie.entity.Movie;
import com.ratingapp.movie.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MovieService {
    
    private final MovieRepository movieRepository;
    private final TmdbApiService tmdbApiService;
    
    @Value("${tmdb.image.base-url}")
    private String imageBaseUrl;
    
    private static final int MOVIES_PER_CATEGORY = 100;
    private static final int PAGES_TO_FETCH = 5;
    
    @Scheduled(cron = "0 0 3 * * MON")
    @Transactional
    public void refreshAllCategories() {
        log.info("Starting weekly refresh of all movie categories");
        
        refreshTrendingMovies();
        refreshNewReleases();
        refreshTopRatedMovies();
        refreshUpcomingMovies();
        
        cleanupOrphanedMovies();
        
        log.info("Weekly refresh completed");
    }
    
    @Transactional
    public void refreshTrendingMovies() {
        log.info("Refreshing trending movies");
        movieRepository.resetTrendingFlag();
        
        List<TmdbMovieDto> movies = fetchMultiplePages(1, PAGES_TO_FETCH, 
            page -> tmdbApiService.getTrendingMovies(page));
        
        processMovies(movies, "trending");
    }
    
    @Transactional
    public void refreshNewReleases() {
        log.info("Refreshing new releases");
        movieRepository.resetNewReleaseFlag();
        
        List<TmdbMovieDto> movies = fetchMultiplePages(1, PAGES_TO_FETCH, 
            page -> tmdbApiService.getNewReleases(page));
        
        processMovies(movies, "newRelease");
    }
    
    @Transactional
    public void refreshTopRatedMovies() {
        log.info("Refreshing top rated movies");
        movieRepository.resetTopRatedFlag();
        
        List<TmdbMovieDto> movies = fetchMultiplePages(1, PAGES_TO_FETCH, 
            page -> tmdbApiService.getTopRatedMovies(page));
        
        processMovies(movies, "topRated");
    }
    
    @Transactional
    public void refreshUpcomingMovies() {
        log.info("Refreshing upcoming movies");
        movieRepository.resetUpcomingFlag();
        
        List<TmdbMovieDto> movies = fetchMultiplePages(1, PAGES_TO_FETCH, 
            page -> tmdbApiService.getUpcomingMovies(page));
        
        processMovies(movies, "upcoming");
    }
    
    private List<TmdbMovieDto> fetchMultiplePages(int startPage, int numberOfPages, PageFetcher pageFetcher) {
        List<TmdbMovieDto> allMovies = new ArrayList<>();
        for (int i = 0; i < numberOfPages; i++) {
            try {
                List<TmdbMovieDto> page = pageFetcher.fetch(startPage + i);
                allMovies.addAll(page);
                if (allMovies.size() >= MOVIES_PER_CATEGORY) break;
            } catch (Exception e) {
                log.error("Failed to fetch page {}", startPage + i, e);
            }
        }
        return allMovies.stream().limit(MOVIES_PER_CATEGORY).collect(Collectors.toList());
    }
    
    @Transactional
    public void processMovies(List<TmdbMovieDto> movieDtos, String category) {
        for (TmdbMovieDto dto : movieDtos) {
            Optional<Movie> existingMovie = movieRepository.findByTmdbId(dto.getId());
            
            Movie movie;
            if (existingMovie.isPresent()) {
                movie = existingMovie.get();
                updateMovieFromDto(movie, dto);
            } else {
                movie = createMovieFromDto(dto);
            }
            
            setCategoryFlag(movie, category, true);
            movieRepository.save(movie);
        }
    }
    
    private Movie createMovieFromDto(TmdbMovieDto dto) {
        Movie movie = new Movie();
        movie.setTmdbId(dto.getId());
        updateMovieFromDto(movie, dto);
        return movie;
    }
    
    private void updateMovieFromDto(Movie movie, TmdbMovieDto dto) {
        movie.setTitle(dto.getTitle());
        movie.setOverview(dto.getOverview());
        movie.setVoteAverage(dto.getVoteAverage());
        
        if (dto.getReleaseDate() != null && !dto.getReleaseDate().isEmpty()) {
            movie.setReleaseDate(LocalDate.parse(dto.getReleaseDate()));
        }
        
        movie.setPosterPath(dto.getPosterPath());
        movie.setGenreIds(dto.getGenreIds());
        movie.setRuntime(dto.getRuntime());
    }
    
    private void setCategoryFlag(Movie movie, String category, boolean value) {
        switch (category) {
            case "trending": movie.setTrending(value); break;
            case "newRelease": movie.setNewRelease(value); break;
            case "topRated": movie.setTopRated(value); break;
            case "upcoming": movie.setUpcoming(value); break;
        }
    }
    
    @Transactional
    public void cleanupOrphanedMovies() {
        movieRepository.deleteMoviesNotInAnyCategory();
    }
    
    public List<MovieResponseDto> getTrendingMovies(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return movieRepository.findByTrendingTrueOrderByVoteAverageDesc(pageRequest)
            .stream()
            .map(movie -> new MovieResponseDto(movie, imageBaseUrl))
            .collect(Collectors.toList());
    }
    
    public List<MovieResponseDto> getNewReleases(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return movieRepository.findByNewReleaseTrueOrderByReleaseDateDesc(pageRequest)
            .stream()
            .map(movie -> new MovieResponseDto(movie, imageBaseUrl))
            .collect(Collectors.toList());
    }
    
    public List<MovieResponseDto> getTopRatedMovies(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return movieRepository.findByTopRatedTrueOrderByVoteAverageDesc(pageRequest)
            .stream()
            .map(movie -> new MovieResponseDto(movie, imageBaseUrl))
            .collect(Collectors.toList());
    }
    
    public List<MovieResponseDto> getUpcomingMovies(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return movieRepository.findByUpcomingTrueOrderByReleaseDateDesc(pageRequest)
            .stream()
            .map(movie -> new MovieResponseDto(movie, imageBaseUrl))
            .collect(Collectors.toList());
    }
    
    public MovieDetailsResponseDto getMovieDetails(Long tmdbId) {
        TmdbMovieDetailsDto tmdbDetails = tmdbApiService.getMovieDetails(tmdbId);
        if (tmdbDetails == null) {
            throw new RuntimeException("Movie not found");
        }
        
        return convertToDetailsResponse(tmdbDetails);
    }
    
    private MovieDetailsResponseDto convertToDetailsResponse(TmdbMovieDetailsDto tmdb) {
        MovieDetailsResponseDto dto = new MovieDetailsResponseDto();
        
        dto.setTmdbId(tmdb.getId());
        dto.setTitle(tmdb.getTitle());
        dto.setOverview(tmdb.getOverview());
        dto.setVoteAverage(tmdb.getVoteAverage());
        dto.setRuntime(tmdb.getRuntime());
        
        if (tmdb.getReleaseDate() != null) {
            LocalDate date = LocalDate.parse(tmdb.getReleaseDate());
            dto.setReleaseDate(date);
        }
        
        dto.setPosterUrl(tmdb.getPosterPath() != null ? 
            imageBaseUrl + "/w500" + tmdb.getPosterPath() : null);
        dto.setBackdropUrl(tmdb.getBackdropPath() != null ? 
            imageBaseUrl + "/w1280" + tmdb.getBackdropPath() : null);
        
        dto.setGenres(tmdb.getGenres().stream()
            .map(TmdbMovieDetailsDto.GenreDto::getName)
            .collect(Collectors.toList()));
        
        dto.setBudget(formatCurrency(tmdb.getBudget()));
        dto.setRevenue(formatCurrency(tmdb.getRevenue()));
        
        dto.setCountries(tmdb.getProductionCountries().stream()
            .map(TmdbMovieDetailsDto.ProductionCountryDto::getName)
            .collect(Collectors.toList()));
        
        if (!tmdb.getSpokenLanguages().isEmpty()) {
            dto.setLanguage(tmdb.getSpokenLanguages().get(0).getName());
        }
        
        dto.setCast(extractTopActors(tmdb.getCredits().getCast(), 4));
        
        List<TmdbMovieDetailsDto.CrewDto> allCrew = tmdb.getCredits().getCrew();
        dto.setDirectors(filterCrewByJob(allCrew, "Director"));
        dto.setWriters(filterCrewByMultipleJobs(allCrew, 
            List.of("Writer", "Screenplay", "Novel", "Story")));
        dto.setProducers(filterCrewByJob(allCrew, "Producer"));
        dto.setComposers(filterCrewByJob(allCrew, "Original Music Composer"));
        dto.setCinematographers(filterCrewByJob(allCrew, "Director of Photography"));
        dto.setEditors(filterCrewByJob(allCrew, "Editor"));
        
        return dto;
    }
    
    private List<ActorDto> extractTopActors(List<TmdbMovieDetailsDto.CastDto> cast, int limit) {
        return cast.stream()
            .filter(c -> c.getProfilePath() != null)
            .sorted(Comparator.comparing(TmdbMovieDetailsDto.CastDto::getOrder))
            .limit(limit)
            .map(c -> {
                ActorDto actor = new ActorDto();
                actor.setName(c.getName());
                actor.setCharacter(c.getCharacter());
                actor.setOrder(c.getOrder());
                actor.setProfileUrl(imageBaseUrl + "/w185" + c.getProfilePath());
                return actor;
            })
            .collect(Collectors.toList());
    }
    
    private List<CrewDto> filterCrewByJob(List<TmdbMovieDetailsDto.CrewDto> crew, String job) {
        return crew.stream()
            .filter(c -> job.equals(c.getJob()))
            .map(this::convertToCrewDto)
            .collect(Collectors.toList());
    }
    
    private List<CrewDto> filterCrewByMultipleJobs(List<TmdbMovieDetailsDto.CrewDto> crew, List<String> jobs) {
        return crew.stream()
            .filter(c -> jobs.contains(c.getJob()))
            .map(this::convertToCrewDto)
            .collect(Collectors.toList());
    }
    
    private CrewDto convertToCrewDto(TmdbMovieDetailsDto.CrewDto tmdbCrew) {
        CrewDto dto = new CrewDto();
        dto.setName(tmdbCrew.getName());
        dto.setJob(tmdbCrew.getJob());
        dto.setProfileUrl(tmdbCrew.getProfilePath() != null ? 
            imageBaseUrl + "/w185" + tmdbCrew.getProfilePath() : null);
        return dto;
    }
    
    private String formatCurrency(Long amount) {
        if (amount == null || amount == 0) return null;
        return "$" + String.format("%,d", amount);
    }
    
    @FunctionalInterface
    private interface PageFetcher {
        List<TmdbMovieDto> fetch(int page);
    }
}