package com.moviebooking.movie_booking.converter;

import com.moviebooking.movie_booking.entity.SeatEntity;
import com.moviebooking.movie_booking.entity.SeatStatusEntity;
import com.moviebooking.movie_booking.entity.SeatTypeEntity;
import com.moviebooking.movie_booking.enums.SeatStatus;
import com.moviebooking.movie_booking.model.dto.RoomSeatDTO;
import com.moviebooking.movie_booking.model.dto.ShowtimeSeatDTO;
import org.springframework.stereotype.Component;

@Component
public class SeatConverter {

    public ShowtimeSeatDTO toSeatDTO(SeatEntity seatEntity, Long showtimeId){
        ShowtimeSeatDTO showtimeSeatDTO = new ShowtimeSeatDTO();
        showtimeSeatDTO.setId(seatEntity.getId());
        showtimeSeatDTO.setColumnIndex(seatEntity.getColumnIndex());
        showtimeSeatDTO.setRowIndex(seatEntity.getRowIndex());

        SeatTypeEntity seatType = seatEntity.getSeatType();
        if (seatEntity.getSeatType() != null) {
            showtimeSeatDTO.setType(seatEntity.getSeatType().getName());
            showtimeSeatDTO.setSurcharge(seatEntity.getSeatType().getSurcharge());
        }

        SeatStatusEntity currentStatus = seatEntity.getStatuses().stream()
                .filter(ss -> ss.getShowtime().getId().equals(showtimeId))
                .findFirst().orElse(null);
        if (currentStatus != null) {
            showtimeSeatDTO.setStatus(currentStatus.getStatus());
        } else {
            showtimeSeatDTO.setStatus(SeatStatus.AVAILABLE);
        }


        return showtimeSeatDTO;
    }
    public RoomSeatDTO toRoomSeatDTO(SeatEntity seatEntity){
        RoomSeatDTO seatDTO = new RoomSeatDTO();
        seatDTO.setId(seatEntity.getId());
        seatDTO.setColumnIndex(seatEntity.getColumnIndex());
        seatDTO.setRowIndex(seatEntity.getRowIndex());

        SeatTypeEntity seatType = seatEntity.getSeatType();
        if (seatEntity.getSeatType() != null) {
            seatDTO.setType(seatEntity.getSeatType().getName());
            seatDTO.setSurcharge(seatEntity.getSeatType().getSurcharge());
        }
        return seatDTO;
    }
}
