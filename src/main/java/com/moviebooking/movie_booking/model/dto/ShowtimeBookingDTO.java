package com.moviebooking.movie_booking.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ShowtimeBookingDTO {

    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private MovieBookingDTO movieBookingDTO;
    private String cinemaName;
    private String roomName;
    private BigDecimal basePrice;
    private List<ShowtimeSeatDTO> seats;
}
