package com.moviebooking.movie_booking.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CinemaDetailDTO {
    private Long id;
    private String name;
    private String address;
    private String email;
    private CityDTO city;
}
