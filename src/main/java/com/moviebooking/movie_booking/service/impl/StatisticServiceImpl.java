package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.model.dto.ChartDataDTO;
import com.moviebooking.movie_booking.model.dto.StatisticDTO;
import com.moviebooking.movie_booking.model.request.StatisticRequest;
import com.moviebooking.movie_booking.model.response.StatisticResponse;
import com.moviebooking.movie_booking.repository.customer.StatisticRepository;
import com.moviebooking.movie_booking.service.StatisticService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StatisticServiceImpl implements StatisticService {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private StatisticRepository statisticRepository;

    public StatisticDTO getDashboardStats(String filterType, Long month, Long year) {
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
        for (Object[] row : result) {
            response.setTotalMovies(row[0] != null ? ((Number) row[0]).longValue() : 0L);
            response.setSoldTickets(row[1] != null ? ((Number) row[1]).longValue() : 0L);
            response.setRevenue(row[2] != null ? ((Number) row[2]).longValue() : 0L);
            response.setAverageEmptySeatRate(row[3] != null ? ((Number) row[3]).doubleValue() : 0.0);
        }

        List<Object[]> chartResults = statisticRepository.getChartData(request);
        List<ChartDataDTO> chartData = new ArrayList<>();
        boolean isMonth = request.getMonth() != null;
        for (Object[] row : chartResults) {
            if (row != null && row.length >= 2 && row[0] != null) {
                String period = String.valueOf(((Number) row[0]).intValue());
                Long tickets = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                Long revenue = (row.length > 2 && row[2] != null) ? ((Number) row[2]).longValue() : 0L;
                String label = isMonth ? ("Ngày " + period) : ("Tháng " + period);
                chartData.add(new ChartDataDTO(period, tickets, revenue, label));
            }
        }
        response.setChartData(chartData);

        return response;
    }
}
