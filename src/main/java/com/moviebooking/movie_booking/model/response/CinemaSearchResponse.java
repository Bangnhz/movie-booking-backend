package com.moviebooking.movie_booking.model.response;

import com.moviebooking.movie_booking.model.dto.CityDTO;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CinemaSearchResponse {
    private Long id;
    private String name;
    private String address;
    private String email;
    private String city;
    private String numberOfRoom;
}
