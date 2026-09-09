package com.moviebooking.movie_booking.model.request.search;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CinemaSearchRequest {
    private String name;
    private Long cityId;
}
