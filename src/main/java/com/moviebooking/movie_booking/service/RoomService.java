package com.moviebooking.movie_booking.service;

import com.moviebooking.movie_booking.model.dto.RoomDTO;
import java.util.List;

public interface RoomService {
    public List<RoomDTO> getRoomsByCinemaId(Long cinemaId);
}
