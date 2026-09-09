package com.moviebooking.movie_booking.model.dto;

import com.moviebooking.movie_booking.enums.AgeRating;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovieBookingDTO {
    private Long id;
    private String title;
    private String posterUrl;
    private String ageRatingName;   // VD: "T18"
    private String ageRatingColor;
}
