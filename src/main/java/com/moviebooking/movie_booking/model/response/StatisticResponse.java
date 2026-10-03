package com.moviebooking.movie_booking.model.response;

import com.moviebooking.movie_booking.model.dto.ChartDataDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StatisticResponse {
    private Long totalMovies;
    private Long soldTickets;
    private Long revenue;
    private Double averageEmptySeatRate;
    private List<ChartDataDTO> chartData;
}
