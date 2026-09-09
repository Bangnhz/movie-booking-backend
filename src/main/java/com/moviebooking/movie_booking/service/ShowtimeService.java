package com.moviebooking.movie_booking.service;

import com.moviebooking.movie_booking.model.dto.ShowtimeBookingDTO;

import java.util.List;

public interface ShowtimeService {
    ShowtimeBookingDTO getBookingDetails(Long showtimeId);
}
