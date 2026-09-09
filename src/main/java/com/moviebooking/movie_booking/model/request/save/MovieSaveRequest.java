package com.moviebooking.movie_booking.model.request.save;

import com.moviebooking.movie_booking.enums.AgeRating;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class MovieSaveRequest {
    private String id;
    private String title;
    private String description;
    private String posterUrl;
    private AgeRating ageRating;
    private LocalDate releaseDate;
    private Integer duration;
    private List<Long> genreIds;
}