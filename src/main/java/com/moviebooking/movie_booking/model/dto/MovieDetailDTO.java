package com.moviebooking.movie_booking.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class MovieDetailDTO {
    private Long id;
    private String title;
    private String description;
    private String posterUrl;
    private String ageRating;
    private LocalDate releaseDate;
    private Integer duration;
    private List<GenreDTO> genres;
}
