package com.moviebooking.movie_booking.service;

import com.moviebooking.movie_booking.entity.CinemaEntity;
import com.moviebooking.movie_booking.model.dto.CityDTO;

import java.util.List;

public interface CityService {
    List<CityDTO> getAllCities();

}
