package com.moviebooking.movie_booking.service;

import com.moviebooking.movie_booking.model.dto.RoomDTO;
import java.util.List;

public interface RoomService {
    List<RoomDTO> getRoomsByCinemaId(Long cinemaId);
    RoomDTO createRoom(RoomDTO roomDTO);
    RoomDTO updateRoom(Long roomId, RoomDTO roomDTO);
    void deleteRoom(Long roomId);
}
