package com.moviebooking.movie_booking.controller;

import com.moviebooking.movie_booking.model.dto.ShowtimeBookingDTO;
import com.moviebooking.movie_booking.service.ShowtimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/showtimes")
public class ShowtimeController {
    @Autowired
    private ShowtimeService showtimeService;

    @GetMapping("/{showtimeId}")
    public ResponseEntity<ShowtimeBookingDTO> getShowtimeDetail(@PathVariable Long showtimeId){
        return ResponseEntity.ok(showtimeService.getBookingDetails(showtimeId));
    }
}
