package com.moviebooking.movie_booking.model;

import com.moviebooking.movie_booking.model.request.BookingRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TempBookingRequest {
    private Long userId;
    private BookingRequest bookingRequest;

    private String tempBookingId;
    private String paymentId;
    private Long bookingId;
}
