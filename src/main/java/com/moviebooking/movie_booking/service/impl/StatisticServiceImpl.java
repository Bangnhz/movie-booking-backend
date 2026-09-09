package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.model.dto.StatisticDTO;
import com.moviebooking.movie_booking.model.request.StatisticRequest;
import com.moviebooking.movie_booking.model.response.StatisticResponse;
import com.moviebooking.movie_booking.repository.customer.StatisticRepository;
import com.moviebooking.movie_booking.service.StatisticService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatisticServiceImpl implements StatisticService {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private StatisticRepository statisticRepository;

    public StatisticDTO getDashboardStats(String filterType,Long month,Long year) {
        // 1. Lấy tổng doanh thu
        String sqlRevenue = "SELECT SUM(total_amount) FROM bookingsb WHERE 1=1";
        Object revResult = entityManager.createNativeQuery(sqlRevenue).getSingleResult();
        Long totalRevenue = (revResult != null) ? ((Number) revResult).longValue() : 0L;

        StringBuilder filterSql = new StringBuilder("");
        if(filterType.equalsIgnoreCase("month") && month != null && year != null){
            filterSql.append(" AND MONTH(");
        }

        String sqlShowtimes = "SELECT COUNT(*) FROM showtimes";
        Long totalShowtimes = ((Number) entityManager.createNativeQuery(sqlShowtimes).getSingleResult()).longValue();

        String sqlTickets = "SELECT COUNT(*) FROM bookings";
        Long totalTickets = ((Number) entityManager.createNativeQuery(sqlTickets).getSingleResult()).longValue();

        String sqlMovies = "SELECT COUNT(*) FROM movies";
        Long totalMovies = ((Number) entityManager.createNativeQuery(sqlMovies).getSingleResult()).longValue();

        return new StatisticDTO(totalRevenue, totalShowtimes, totalTickets, totalMovies);
    }

    @Override
    public StatisticResponse getStatistic(StatisticRequest request) {
        List<Object[]> result = statisticRepository.getStatistics(request);
        StatisticResponse response = new StatisticResponse();
        for(Object[] row : result){
            response.setTotalMovies(((Number) row[0]).longValue());
            response.setSoldTickets(((Number) row[1]).longValue());
            response.setRevenue(((Number) row[2]).longValue());
            response.setAverageEmptySeatRate(((Number) row[3]).doubleValue());
        }
        return response;
    }
}
