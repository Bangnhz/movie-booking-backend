package com.moviebooking.movie_booking.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatisticResponse {
    private Long totalMovies;
    private Long soldTickets;
    private Long revenue;
    private Double averageEmptySeatRate;

}
