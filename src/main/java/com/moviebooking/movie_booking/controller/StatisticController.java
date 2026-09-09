package com.moviebooking.movie_booking.controller;

import com.moviebooking.movie_booking.model.dto.StatisticDTO;
import com.moviebooking.movie_booking.model.request.StatisticRequest;
import com.moviebooking.movie_booking.model.response.StatisticResponse;
import com.moviebooking.movie_booking.service.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistics")
public class StatisticController {
    @Autowired
    private StatisticService statisticService;

    @GetMapping
    public ResponseEntity<StatisticResponse> getStatistics(@ModelAttribute StatisticRequest statisticRequest) {
        return ResponseEntity.ok(statisticService.getStatistic(statisticRequest));
    }

}
