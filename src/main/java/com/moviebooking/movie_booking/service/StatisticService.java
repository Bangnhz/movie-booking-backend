package com.moviebooking.movie_booking.service;

import com.moviebooking.movie_booking.model.dto.StatisticDTO;
import com.moviebooking.movie_booking.model.request.StatisticRequest;
import com.moviebooking.movie_booking.model.response.StatisticResponse;

import java.util.List;

public interface StatisticService {
    public StatisticResponse getStatistic(StatisticRequest statisticRequest);
}
