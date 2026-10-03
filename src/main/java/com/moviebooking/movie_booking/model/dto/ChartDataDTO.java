package com.moviebooking.movie_booking.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChartDataDTO {
    private String period;
    private Long value;
    private Long revenue;
    private String label;
}
