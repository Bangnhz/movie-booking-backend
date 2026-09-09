package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.converter.SeatConverter;
import com.moviebooking.movie_booking.converter.ShowtimeConverter;
import com.moviebooking.movie_booking.entity.ShowtimeEntity;
import com.moviebooking.movie_booking.model.dto.ShowtimeBookingDTO;
import com.moviebooking.movie_booking.repository.ShowtimeRepository;
import com.moviebooking.movie_booking.service.ShowtimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShowtimeServciceImpl implements ShowtimeService {
    @Autowired
    private ShowtimeConverter showtimeConverter;
    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Override
    public ShowtimeBookingDTO getBookingDetails(Long showtimeId) {
        ShowtimeEntity showtimeEntity = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Showtime not found with id: " + showtimeId));
        return showtimeConverter.toShowtimeBookingDTO(showtimeEntity);
    }
}
