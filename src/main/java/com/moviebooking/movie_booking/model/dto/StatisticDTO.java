package com.moviebooking.movie_booking.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class  StatisticDTO {
    private Long totalRevenue;
    private Long totalShowtimes;
    private Long totalTickets;
    private Long totalMovies;

}