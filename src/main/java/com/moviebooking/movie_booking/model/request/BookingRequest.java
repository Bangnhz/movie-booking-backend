package com.moviebooking.movie_booking.model.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class BookingRequest {
    private Long userId;
    private Long showtimeId;
    private List<Long> seatIds;
    private LocalDateTime createdAt;
}