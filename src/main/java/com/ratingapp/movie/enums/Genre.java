package com.ratingapp.movie.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Genre {
    ACTION(28, "Боевик"),
    ADVENTURE(12, "Приключения"),
    ANIMATION(16, "Мультфильм"),
    COMEDY(35, "Комедия"),
    CRIME(80, "Криминал"),
    DOCUMENTARY(99, "Документальный"),
    DRAMA(18, "Драма"),
    FAMILY(10751, "Семейный"),
    FANTASY(14, "Фэнтези"),
    HISTORY(36, "История"),
    HORROR(27, "Ужасы"),
    MUSIC(10402, "Музыка"),
    MYSTERY(9648, "Детектив"),
    ROMANCE(10749, "Мелодрама"),
    SCIENCE_FICTION(878, "Фантастика"),
    TV_MOVIE(10770, "Телевизионный фильм"),
    THRILLER(53, "Триллер"),
    WAR(10752, "Военный"),
    WESTERN(37, "Вестерн"),
    ACTION_N_ADVENTURE(10759, "Боевик и Приключения"),
    CHILDREN(10762,"Детский"),
    NEWS(10763,"Новости"),
    REALITY_SHOW(10764,"Реалити-шоу"),
    SF_M_FANTASY(10765,"НФ и Фэнтези"),
    SOAP_OPERA(10766,"Мыльная опера"),
    TALK_SHOW(10767,"Ток-шоу"),
    WAR_N_POLITICS(10768,"Война и Политика");

    private final int tmdbId;
    private final String displayName;

    Genre(int tmdbId, String displayName) {
        this.tmdbId = tmdbId;
        this.displayName = displayName;
    }

    public int getTmdbId() {
        return tmdbId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Genre fromTmdbId(int tmdbId) {
        return Arrays.stream(values())
            .filter(genre -> genre.tmdbId == tmdbId)
            .findFirst()
            .orElse(null);
    }

    public static List<Integer> toTmdbIds(List<Genre> genres) {
        return genres.stream()
            .map(Genre::getTmdbId)
            .collect(Collectors.toList());
    }

    public static List<String> toDisplayNames(List<Integer> genreIds) {
        return genreIds.stream()
            .map(Genre::fromTmdbId)
            .filter(genre -> genre != null)
            .map(Genre::getDisplayName)
            .collect(Collectors.toList());
    }
}