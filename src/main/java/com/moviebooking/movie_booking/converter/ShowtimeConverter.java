package com.moviebooking.movie_booking.converter;

import com.moviebooking.movie_booking.entity.SeatEntity;
import com.moviebooking.movie_booking.entity.ShowtimeEntity;
import com.moviebooking.movie_booking.model.dto.ShowtimeSeatDTO;
import com.moviebooking.movie_booking.model.dto.ShowtimeBookingDTO;
import com.moviebooking.movie_booking.service.RedisService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class ShowtimeConverter {
    @Autowired
    private SeatConverter seatConverter;
    @Autowired
    private MovieConverter movieConverter;
    @Autowired(required = false)
    private RedisService redisService;

    @Autowired
    private ModelMapper modelMapper;

    public ShowtimeBookingDTO toShowtimeBookingDTO(ShowtimeEntity showtimeEntity){
        ShowtimeBookingDTO showtimeBookingDTO = modelMapper.map(showtimeEntity,ShowtimeBookingDTO.class);

        List<SeatEntity> seatsInRoom = showtimeEntity.getRoom().getSeats();
        Set<Long> heldSeatIds = Collections.emptySet();
        if (redisService != null && showtimeEntity.getId() != null && seatsInRoom != null) {
            List<Long> seatIds = seatsInRoom.stream().map(SeatEntity::getId).toList();
            heldSeatIds = redisService.getHeldSeatIds(showtimeEntity.getId(), seatIds);
        }
        final Set<Long> finalHeldSeatIds = heldSeatIds;

        List<ShowtimeSeatDTO> showtimeSeatDTOS = seatsInRoom != null
                ? seatsInRoom.stream()
                    .map(seatEntity -> seatConverter.toSeatDTO(seatEntity, showtimeEntity.getId(), finalHeldSeatIds))
                    .toList()
                : Collections.emptyList();
        showtimeBookingDTO.setSeats(showtimeSeatDTOS);
        showtimeBookingDTO.setMovieBookingDTO(movieConverter.toMovieBookingDTO(showtimeEntity.getMovie()));

        showtimeBookingDTO.setCinemaName(showtimeEntity.getRoom().getCinema().getName());
        showtimeBookingDTO.setRoomName(showtimeEntity.getRoom().getName());
        return showtimeBookingDTO;
    }
}
