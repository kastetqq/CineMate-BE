package com.ratingapp.movie.dto;

import com.ratingapp.movie.entity.Actor;
import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class ActorResponse {
    private String id;
    private Long tmdbId;
    private String name;
    private String profileUrl;
    private Integer order;

    public ActorResponse(Actor actor, String imageBaseUrl) {
        this.id = actor.getId() != null ? actor.getId().toString() : null;
        this.tmdbId = actor.getTmdbId();
        this.name = actor.getName();
        this.profileUrl = actor.getProfilePath() != null ? imageBaseUrl + "/w185" + actor.getProfilePath() : null;
        this.order = actor.getOrder();
    }
}