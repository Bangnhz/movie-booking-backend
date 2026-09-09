package com.moviebooking.movie_booking.controller;

import com.moviebooking.movie_booking.model.dto.CityDTO;
import com.moviebooking.movie_booking.repository.CityRepository;
import com.moviebooking.movie_booking.service.CityService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@CrossOrigin(origins = "*")

public class CityController {
    @Autowired
    private CityService cityService;

    @GetMapping
    public ResponseEntity<?> getCities() {
        List<CityDTO> cities = cityService.getAllCities();
        return ResponseEntity.ok(cities);
    }
}
