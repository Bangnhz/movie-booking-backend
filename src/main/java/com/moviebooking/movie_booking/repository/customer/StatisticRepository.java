package com.moviebooking.movie_booking.repository.customer;
import com.moviebooking.movie_booking.model.request.StatisticRequest;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface StatisticRepository {
    public List<Object[]> getStatistics(StatisticRequest request);
}
