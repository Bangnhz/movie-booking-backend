package com.moviebooking.movie_booking.service;

import com.moviebooking.movie_booking.model.dto.RoomSeatDTO;
import com.moviebooking.movie_booking.model.dto.ShowtimeSeatDTO;

import java.util.List;

public interface SeatService {
    List<RoomSeatDTO> getSeatsByRoomId(Long roomId);
}
