package com.moviebooking.movie_booking.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
public class RoomSeatDTO {
    private Long id;
    private String rowIndex;
    private Integer columnIndex;
    private String type;
    private BigDecimal surcharge;
}
