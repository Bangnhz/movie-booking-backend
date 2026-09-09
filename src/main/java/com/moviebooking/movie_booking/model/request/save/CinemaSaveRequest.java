package com.moviebooking.movie_booking.model.request.save;

import com.moviebooking.movie_booking.model.dto.CityDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CinemaSaveRequest {
    private String name;
    private String address;
    private String email;
    private CityDTO city;
}
