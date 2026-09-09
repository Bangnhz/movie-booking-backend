package com.moviebooking.movie_booking.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MovieScheduleDTO {
    private Long id;
    String title;
    String posterUrl;
    String ageRating;
    List<ShowtimeSlotDTO> showtimes;
}
