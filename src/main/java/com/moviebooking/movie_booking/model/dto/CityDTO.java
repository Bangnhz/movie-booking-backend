package com.moviebooking.movie_booking.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CityDTO {
    private Long id;
    private String name;
    List<CinemaScheduleDTO> cinemas;
}
