package com.moviebooking.movie_booking.converter;

import com.moviebooking.movie_booking.entity.SeatEntity;
import com.moviebooking.movie_booking.entity.ShowtimeEntity;
import com.moviebooking.movie_booking.model.dto.ShowtimeSeatDTO;
import com.moviebooking.movie_booking.model.dto.ShowtimeBookingDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShowtimeConverter {
    @Autowired
    private SeatConverter seatConverter;
    @Autowired
    private MovieConverter movieConverter;

    @Autowired
    private ModelMapper modelMapper;

    public ShowtimeBookingDTO toShowtimeBookingDTO(ShowtimeEntity showtimeEntity){
        ShowtimeBookingDTO showtimeBookingDTO = modelMapper.map(showtimeEntity,ShowtimeBookingDTO.class);

        List<SeatEntity> seatsInRoom = showtimeEntity.getRoom().getSeats();
        List<ShowtimeSeatDTO> showtimeSeatDTOS = seatsInRoom.stream()
                .map(seatEntity -> seatConverter.toSeatDTO(seatEntity,showtimeEntity.getId()))
                .toList();
        showtimeBookingDTO.setSeats(showtimeSeatDTOS);
        showtimeBookingDTO.setMovieBookingDTO(movieConverter.toMovieBookingDTO(showtimeEntity.getMovie()));

        showtimeBookingDTO.setCinemaName(showtimeEntity.getRoom().getCinema().getName());
        showtimeBookingDTO.setRoomName(showtimeEntity.getRoom().getName());
        return showtimeBookingDTO;
    }
}
