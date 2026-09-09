package com.moviebooking.movie_booking.model.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class StatisticRequest {
    private Integer month;
    private Integer year;
    private Long movieId;
    private Long cinemaId;
}
