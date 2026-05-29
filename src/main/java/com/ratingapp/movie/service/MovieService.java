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
    private static final int UPCOMING_MOVIES_LIMIT = 5;
    private static final int PAGES_TO_FETCH = 5;
    
    @Scheduled(cron = "0 0 3 */15 * *")
    @Transactional
    public void refreshAllCategories() {
        log.info("Starting refresh of all movie categories (every 15 days)");
        
        refreshTrendingMovies();
        refreshNewReleases();
        refreshTopRatedMovies();
        refreshUpcomingMovies();
        
        cleanupOrphanedMovies();
        
        log.info("Refresh completed");
    }
    
    @Transactional
    public void refreshTrendingMovies() {
        log.info("Refreshing trending movies");
        movieRepository.resetTrendingFlag();
        
        List<TmdbMovieDto> movies = fetchMultiplePages(1, PAGES_TO_FETCH, 
            page -> tmdbApiService.getTrendingMovies(page));
        
        movies = movies.stream()
            .filter(this::isMovieValid)
            .filter(m -> hasValidTitle(m.getTitle()))
            .limit(MOVIES_PER_CATEGORY)
            .collect(Collectors.toList());
        
        processMovies(movies, "trending");
    }
    
    @Transactional
    public void refreshNewReleases() {
        log.info("Refreshing new releases");
        movieRepository.resetNewReleaseFlag();
        
        List<TmdbMovieDto> movies = fetchMultiplePages(1, PAGES_TO_FETCH, 
            page -> tmdbApiService.getNewReleases(page));
        
        movies = movies.stream()
            .filter(this::isMovieValid)
            .filter(m -> m.getVoteAverage() != null && m.getVoteAverage() > 0)
            .filter(m -> hasValidTitle(m.getTitle()))
            .limit(MOVIES_PER_CATEGORY)
            .collect(Collectors.toList());
        
        processMovies(movies, "newRelease");
    }
    
    @Transactional
    public void refreshTopRatedMovies() {
        log.info("Refreshing top rated movies");
        movieRepository.resetTopRatedFlag();
        
        List<TmdbMovieDto> movies = fetchMultiplePages(1, PAGES_TO_FETCH, 
            page -> tmdbApiService.getTopRatedMovies(page));
        
        movies = movies.stream()
            .filter(this::isMovieValid)
            .limit(MOVIES_PER_CATEGORY)
            .collect(Collectors.toList());
        
        processMovies(movies, "topRated");
    }
    
    @Transactional
    public void refreshUpcomingMovies() {
        log.info("Refreshing upcoming movies");
        movieRepository.resetUpcomingFlag();
        
        List<TmdbMovieDto> allMovies = new ArrayList<>();
        int page = 1;
        int maxPages = 10;
        
        while (allMovies.size() < UPCOMING_MOVIES_LIMIT * 3 && page <= maxPages) {
            try {
                List<TmdbMovieDto> movies = tmdbApiService.getUpcomingMovies(page);
                allMovies.addAll(movies);
                page++;
            } catch (Exception e) {
                log.error("Failed to fetch upcoming movies page {}", page, e);
                page++;
            }
        }
        
        LocalDate today = LocalDate.now();
        LocalDate minDate = today.plusDays(16);
        LocalDate maxDate = today.plusMonths(6);
        
        List<TmdbMovieDto> validMovies = allMovies.stream()
            .filter(this::isMovieValid)
            .filter(m -> {
                if (m.getReleaseDate() == null || m.getReleaseDate().isEmpty()) {
                    return false;
                }
                try {
                    LocalDate releaseDate = LocalDate.parse(m.getReleaseDate());
                    return !releaseDate.isBefore(minDate) && !releaseDate.isAfter(maxDate);
                } catch (Exception e) {
                    return false;
                }
            })
            .filter(m -> hasValidTitle(m.getTitle()))
            .sorted((m1, m2) -> {
                double pop1 = m1.getPopularity() != null ? m1.getPopularity() : 0;
                double pop2 = m2.getPopularity() != null ? m2.getPopularity() : 0;
                return Double.compare(pop2, pop1);
            })
            .limit(UPCOMING_MOVIES_LIMIT)
            .collect(Collectors.toList());
        
        log.info("Found {} upcoming movies in date range {} to {}", validMovies.size(), minDate, maxDate);
        
        processMovies(validMovies, "upcoming");
    }
    
    private boolean isMovieValid(TmdbMovieDto movie) {
        return movie != null &&
               movie.getTitle() != null && !movie.getTitle().trim().isEmpty() &&
               movie.getOverview() != null && !movie.getOverview().trim().isEmpty() &&
               movie.getPosterPath() != null && !movie.getPosterPath().trim().isEmpty();
    }

    private boolean hasValidTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return false;
        }
        
        if (title.matches("^\\d+$")) {
            return false;
        }
        
        return title.matches("[а-яА-ЯёЁa-zA-Z0-9\\s\\-.,!?:;\"'()]+");
    }
    
    private List<TmdbMovieDto> fetchMultiplePages(int startPage, int numberOfPages, PageFetcher pageFetcher) {
        List<TmdbMovieDto> allMovies = new ArrayList<>();
        Set<Long> seenIds = new HashSet<>();
        
        for (int i = 0; i < numberOfPages; i++) {
            try {
                List<TmdbMovieDto> page = pageFetcher.fetch(startPage + i);
                
                List<TmdbMovieDto> uniqueMovies = page.stream()
                    .filter(m -> m.getId() != null)
                    .filter(m -> !seenIds.contains(m.getId()))
                    .peek(m -> seenIds.add(m.getId()))
                    .collect(Collectors.toList());
                
                allMovies.addAll(uniqueMovies);
                
                if (allMovies.size() >= MOVIES_PER_CATEGORY) {
                    allMovies = allMovies.stream().limit(MOVIES_PER_CATEGORY).collect(Collectors.toList());
                    break;
                }
            } catch (Exception e) {
                log.error("Failed to fetch page {}", startPage + i, e);
            }
        }
        return allMovies;
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
            
            if (movie.getMovieTrailerUrl() == null || movie.getMovieTrailerUrl().isEmpty()) {
                refreshMovieTrailer(dto.getId());
            }
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
        movie.setPopularity(dto.getPopularity());
        
        if (dto.getReleaseDate() != null && !dto.getReleaseDate().isEmpty()) {
            try {
                movie.setReleaseDate(LocalDate.parse(dto.getReleaseDate()));
            } catch (Exception e) {
                log.debug("Invalid release date for movie {}: {}", dto.getId(), dto.getReleaseDate());
            }
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
    
    public List<MovieResponseDto> getAllMovies() {
        log.info("Fetching all movies from all categories");
        List<Movie> movies = movieRepository.findAllMoviesInCategories();
        log.info("Found {} unique movies across all categories", movies.size());
        return movies.stream()
            .map(movie -> new MovieResponseDto(movie, imageBaseUrl))
            .collect(Collectors.toList());
    }
    
    public List<MovieResponseDto> searchMovies(String query, int page, int size) {

        int tmdbPage = page + 1;
        List<TmdbMovieDto> tmdbMovies = tmdbApiService.searchMovies(query, tmdbPage);
        
        return tmdbMovies.stream()
            .limit(size)
            .map(dto -> new MovieResponseDto(dto, imageBaseUrl))
            .collect(Collectors.toList());
    }
    
    public MovieDetailsResponseDto getMovieDetails(Long tmdbId) {
        TmdbMovieDetailsDto tmdbDetails = tmdbApiService.getMovieDetails(tmdbId);
        if (tmdbDetails == null) {
            throw new RuntimeException("Movie not found");
        }
        
        return convertToDetailsResponse(tmdbDetails);
    }

    @Transactional
    public void refreshMovieTrailer(Long tmdbId) {
        log.info("Refreshing trailer for movie: {}", tmdbId);
    
        TmdbVideoDto trailer = tmdbApiService.getMovieTrailer(tmdbId);
    
        if (trailer == null) {
            return;
        }
        String embedUrl = "https://www.youtube.com/embed/" + trailer.getKey();

        movieRepository.findByTmdbId(tmdbId).ifPresent(movie -> {
            movie.setMovieTrailerUrl(embedUrl);
            movieRepository.save(movie);
            log.info("Trailer saved for movie: {}", movie.getTitle());
        });
    }

    private String getTrailerUrlFromDb(Long tmdbId) {
        return movieRepository.findByTmdbId(tmdbId)
        .map(Movie::getMovieTrailerUrl)
        .orElse(null);
    }   
    
    private MovieDetailsResponseDto convertToDetailsResponse(TmdbMovieDetailsDto tmdb) {
        MovieDetailsResponseDto dto = new MovieDetailsResponseDto();
        
        dto.setTmdbId(tmdb.getId());
        dto.setTitle(tmdb.getTitle());
        dto.setOverview(tmdb.getOverview());
        dto.setVoteAverage(tmdb.getVoteAverage());
        dto.setPopularity(tmdb.getPopularity());
        dto.setRuntime(tmdb.getRuntime());
        dto.setTrailerUrl(getTrailerUrlFromDb(tmdb.getId()));
        
        if (tmdb.getReleaseDate() != null) {
            try {
                LocalDate date = LocalDate.parse(tmdb.getReleaseDate());
                dto.setReleaseDate(date);
            } catch (Exception e) {
                log.debug("Invalid release date format: {}", tmdb.getReleaseDate());
            }
        }
        
        dto.setPosterUrl(tmdb.getPosterPath() != null ? 
            imageBaseUrl + "/w500" + tmdb.getPosterPath() : null);
        dto.setBackdropUrl(tmdb.getBackdropPath() != null ? 
            imageBaseUrl + "/w1280" + tmdb.getBackdropPath() : null);
        
        if (tmdb.getGenres() != null) {
            dto.setGenres(tmdb.getGenres().stream()
                .map(TmdbMovieDetailsDto.GenreDto::getName)
                .collect(Collectors.toList()));
        }
        
        dto.setBudget(formatCurrency(tmdb.getBudget()));
        dto.setRevenue(formatCurrency(tmdb.getRevenue()));
        
        if (tmdb.getProductionCountries() != null) {
            dto.setCountries(tmdb.getProductionCountries().stream()
                .map(TmdbMovieDetailsDto.ProductionCountryDto::getName)
                .collect(Collectors.toList()));
        }
        
        if (tmdb.getSpokenLanguages() != null && !tmdb.getSpokenLanguages().isEmpty()) {
            dto.setLanguage(tmdb.getSpokenLanguages().get(0).getName());
        }
        
        if (tmdb.getCredits() != null) {
            if (tmdb.getCredits().getCast() != null) {
                dto.setCast(extractTopActors(tmdb.getCredits().getCast(), 4));
            }
            
            if (tmdb.getCredits().getCrew() != null) {
                List<TmdbMovieDetailsDto.CrewDto> allCrew = tmdb.getCredits().getCrew();
                dto.setDirectors(filterCrewByJob(allCrew, "Director"));
                dto.setWriters(filterCrewByMultipleJobs(allCrew, 
                    List.of("Writer", "Screenplay", "Novel", "Story")));
                dto.setProducers(filterCrewByJob(allCrew, "Producer"));
                dto.setComposers(filterCrewByJob(allCrew, "Original Music Composer"));
                dto.setCinematographers(filterCrewByJob(allCrew, "Director of Photography"));
                dto.setEditors(filterCrewByJob(allCrew, "Editor"));
            }
        }
        
        return dto;
    }
    
    private List<ActorDto> extractTopActors(List<TmdbMovieDetailsDto.CastDto> cast, int limit) {
        if (cast == null) return Collections.emptyList();
        
        return cast.stream()
            .filter(c -> c.getName() != null)
            .sorted(Comparator.comparing(TmdbMovieDetailsDto.CastDto::getOrder))
            .limit(limit)
            .map(c -> {
                ActorDto actor = new ActorDto();
                actor.setTmdbId(c.getTmdbId());
                actor.setName(c.getName());
                actor.setCharacter(c.getCharacter());
                actor.setOrder(c.getOrder());
                actor.setProfilePath(c.getProfilePath());
                actor.setProfileUrl(c.getProfilePath() != null ? 
                    imageBaseUrl + "/w185" + c.getProfilePath() : null);
                return actor;
            })
            .collect(Collectors.toList());
    }
    
    private List<CrewDto> filterCrewByJob(List<TmdbMovieDetailsDto.CrewDto> crew, String job) {
        if (crew == null) return Collections.emptyList();
        
        return crew.stream()
            .filter(c -> job.equals(c.getJob()))
            .map(this::convertToCrewDto)
            .collect(Collectors.toList());
    }
    
    private List<CrewDto> filterCrewByMultipleJobs(List<TmdbMovieDetailsDto.CrewDto> crew, List<String> jobs) {
        if (crew == null) return Collections.emptyList();
        
        return crew.stream()
            .filter(c -> jobs.contains(c.getJob()))
            .map(this::convertToCrewDto)
            .collect(Collectors.toList());
    }

    private CrewDto convertToCrewDto(TmdbMovieDetailsDto.CrewDto tmdbCrew) {
        CrewDto dto = new CrewDto();
        dto.setTmdbId(tmdbCrew.getTmdbId());
        dto.setName(tmdbCrew.getName());
        dto.setJob(tmdbCrew.getJob());
        dto.setProfilePath(tmdbCrew.getProfilePath());
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