package com.moviebooking.movie_booking.service;

import com.moviebooking.movie_booking.model.request.BookingRequest;
import com.moviebooking.movie_booking.model.TempBookingRequest;

import java.util.List;

public interface RedisService {
    boolean holdSeats(Long showtimeId, List<Long> seatIds, Long userId, int minutes);
    void releaseSeats(Long showtimeId, List<Long> seatIds);
    void releaseSeats(Long showtimeId, List<Long> seatIds, Long userId);
    boolean reserveSeats(Long showtimeId, List<Long> seatIds, Long userId, int minutes);
    String saveTempBooking(BookingRequest bookingRequest, Long userId, int minutes);
    TempBookingRequest getTempBooking(String bookingId);
    void updateTempBookingPaymentId(String bookingId, String paymentId);
    void updateTempBookingBookingId(String tempBookingId, Long savedBookingId);
    void saveTxnTempMapping(String txnRef, String tempBookingId, int minutes);
    String getTempBookingIdByTxnRef(String txnRef);
    boolean isOwnsSeats(Long showtimeId, List<Long> seatIds, Long userId);
    boolean isSeatHeld(Long showtimeId, Long seatId);
    java.util.Set<Long> getHeldSeatIds(Long showtimeId, List<Long> seatIds);
    void deleteTempBooking(String bookingId);
}
