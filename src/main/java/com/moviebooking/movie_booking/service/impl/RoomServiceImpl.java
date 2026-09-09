package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.converter.RoomConverter;
import com.moviebooking.movie_booking.entity.RoomEntity;
import com.moviebooking.movie_booking.model.dto.RoomDTO;
import com.moviebooking.movie_booking.repository.RoomRepository;
import com.moviebooking.movie_booking.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private RoomConverter roomConverter;
    @Override
    public List<RoomDTO> getRoomsByCinemaId(Long cinemaId) {
        List<RoomEntity> roomEntities = roomRepository.findAllByCinemaId(cinemaId);

        return roomEntities.stream().map(roomEntity -> roomConverter.toRoomDTO(roomEntity)).toList();
    }
}
