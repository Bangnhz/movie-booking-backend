package com.moviebooking.movie_booking.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MovieCardDTO {
    private Long id;
    private String title;
    private String posterUrl;
    private String ageRating;
    private LocalDate releaseDate;
    private Integer duration;
}
