package com.ratingapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = {"com.ratingapp.movie.entity", "com.ratingapp.auth.entity", "com.ratingapp.watchlist.entity"})
@EnableJpaRepositories(basePackages = {"com.ratingapp.movie.repository", "com.ratingapp.auth.repository", "com.ratingapp.watchlist.repository"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}