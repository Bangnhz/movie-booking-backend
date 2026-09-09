package com.moviebooking.movie_booking.repository;

import com.moviebooking.movie_booking.entity.SeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SeatRepository extends JpaRepository<SeatEntity,Long> {
    List<SeatEntity> getSeatsByRoomId(Long roomId);
}
