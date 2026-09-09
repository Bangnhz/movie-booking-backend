package com.moviebooking.movie_booking.service;

import com.moviebooking.movie_booking.model.request.BookingRequest;

import java.util.List;

public interface RedisService {
    boolean holdSeats(Long showtimeId, List<Long> seatIds, Long userId, int minutes);
    void releaseSeats(Long showtimeId, List<Long> seatIds);
    String saveTempBooking(BookingRequest bookingRequest, int minutes);
    BookingRequest getTempBooking(String bookingId);
}
