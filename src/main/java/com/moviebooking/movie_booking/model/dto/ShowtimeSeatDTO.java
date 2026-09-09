package com.moviebooking.movie_booking.model.dto;

import com.moviebooking.movie_booking.enums.SeatStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ShowtimeSeatDTO {
    private Long id;
    private String rowIndex;
    private Integer columnIndex;
    private String type;
    private BigDecimal surcharge;
    private SeatStatus status;
}
