package com.moviebooking.movie_booking.service;

import com.moviebooking.movie_booking.model.request.BookingRequest;
import com.moviebooking.movie_booking.model.request.search.BookingSearchRequest;
import com.moviebooking.movie_booking.model.response.BookingResponse;
import com.moviebooking.movie_booking.model.response.BookingSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookingService {
    Long create(BookingRequest bookingRequest, String username);
    void delete(Long id);
    String holdBooking(BookingRequest bookingRequest, Long userId);
//    BookingResponse getBookingResponse(Long bookingId);
    BookingResponse getTempBookingResponse(String uuid, Long userId);
    Page<BookingSearchResponse> getBookingsByUserId(Long userId, BookingSearchRequest request, Pageable pageable);
    Long createPendingFromTemp(String tempBookingId, Long userId);
    void processPaymentResult(String transactionCode, boolean isSuccess);
}