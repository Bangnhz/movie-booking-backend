package com.moviebooking.movie_booking.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CinemaScheduleDTO {
    private Long id;
    private String name;
    private String address;
    private String email;
    private List<MovieScheduleDTO> movies;
}
