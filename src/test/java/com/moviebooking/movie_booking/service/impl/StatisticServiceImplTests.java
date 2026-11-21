package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.model.dto.ChartDataDTO;
import com.moviebooking.movie_booking.model.request.StatisticRequest;
import com.moviebooking.movie_booking.model.response.StatisticResponse;
import com.moviebooking.movie_booking.repository.customer.StatisticRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StatisticServiceImplTests {

    @Mock
    private StatisticRepository statisticRepository;

    @InjectMocks
    private StatisticServiceImpl statisticService;

    private StatisticRequest request;

    @BeforeEach
    void setUp() {
        request = new StatisticRequest();
        request.setYear(2026);
        request.setMonth(11);
        request.setMovieId(5L);
        request.setCinemaId(2L);
    }

    @Test
    void getStatistic_MapsRowsAndChartCorrectly() {
        List<Object[]> statRows = new ArrayList<>();
        statRows.add(new Object[]{10L, 150L, 15000000L, 25.5});
        when(statisticRepository.getStatistics(request)).thenReturn(statRows);

        List<Object[]> chartRows = new ArrayList<>();
        chartRows.add(new Object[]{1, 15L, 1500000L});
        chartRows.add(new Object[]{2, 20L, 2000000L});
        when(statisticRepository.getChartData(request)).thenReturn(chartRows);

        StatisticResponse response = statisticService.getStatistic(request);

        assertNotNull(response);
        assertEquals(10L, response.getTotalMovies());
        assertEquals(150L, response.getSoldTickets());
        assertEquals(15000000L, response.getRevenue());
        assertEquals(25.5, response.getAverageEmptySeatRate(), 0.001);

        assertNotNull(response.getChartData());
        assertEquals(2, response.getChartData().size());
        assertEquals("Ngày 1", response.getChartData().get(0).getLabel());
        assertEquals(15L, response.getChartData().get(0).getValue());
        assertEquals(1500000L, response.getChartData().get(0).getRevenue());

        verify(statisticRepository).getStatistics(request);
        verify(statisticRepository).getChartData(request);
    }

    @Test
    void getStatistic_HandlesNullValuesGracefully() {
        List<Object[]> statRows = new ArrayList<>();
        statRows.add(new Object[]{null, null, null, null});
        when(statisticRepository.getStatistics(request)).thenReturn(statRows);
        when(statisticRepository.getChartData(request)).thenReturn(new ArrayList<>());

        StatisticResponse response = statisticService.getStatistic(request);

        assertNotNull(response);
        assertEquals(0L, response.getTotalMovies());
        assertEquals(0L, response.getSoldTickets());
        assertEquals(0L, response.getRevenue());
        assertEquals(0.0, response.getAverageEmptySeatRate(), 0.001);
        assertTrue(response.getChartData().isEmpty());
    }
}
