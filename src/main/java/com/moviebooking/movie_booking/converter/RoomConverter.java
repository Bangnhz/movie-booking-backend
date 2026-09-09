package com.moviebooking.movie_booking.converter;

import com.moviebooking.movie_booking.entity.RoomEntity;
import com.moviebooking.movie_booking.model.dto.RoomDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RoomConverter {
    @Autowired
    private ModelMapper modelMapper;

    public RoomDTO toRoomDTO(RoomEntity roomEntity) {
        return modelMapper.map(roomEntity, RoomDTO.class);
    }
}
