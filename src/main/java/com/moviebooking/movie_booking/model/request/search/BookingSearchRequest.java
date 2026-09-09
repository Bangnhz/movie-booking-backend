package com.moviebooking.movie_booking.model.request.search;

import com.moviebooking.movie_booking.enums.BookingStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BookingSearchRequest {

    private String cinemaName;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BookingStatus status;
}