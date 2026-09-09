package com.moviebooking.movie_booking.controller;

import com.moviebooking.movie_booking.enums.AgeRating;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/age-ratings")
public class AgeRatingController {
    @GetMapping
    public ResponseEntity<?> getAgeRatings() {
        return ResponseEntity.ok(
                Arrays.stream(AgeRating.values())
                        .map(rating -> Map.of(
                                "name", rating.name(),
                                "description", rating.getDescription(),
                                "color", rating.getColor()
                        )).toList()
        );
    }
}
