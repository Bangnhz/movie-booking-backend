package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.converter.SeatConverter;
import com.moviebooking.movie_booking.entity.SeatEntity;
import com.moviebooking.movie_booking.model.dto.RoomSeatDTO;
import com.moviebooking.movie_booking.model.dto.ShowtimeSeatDTO;
import com.moviebooking.movie_booking.repository.SeatRepository;
import com.moviebooking.movie_booking.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class SeatServiceImpl implements SeatService {

    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private SeatConverter seatConverter;
    @Override
    public List<RoomSeatDTO> getSeatsByRoomId(Long roomId) {
        List<SeatEntity> seatEntities = seatRepository.getSeatsByRoomId(roomId);

        return seatEntities.stream().map(seatConverter::toRoomSeatDTO).toList();
    }
}
