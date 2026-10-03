package com.moviebooking.movie_booking.repository.customer;

import com.moviebooking.movie_booking.model.request.StatisticRequest;
import java.util.List;

public interface StatisticRepository {
    List<Object[]> getStatistics(StatisticRequest request);
    List<Object[]> getChartData(StatisticRequest request);
}
