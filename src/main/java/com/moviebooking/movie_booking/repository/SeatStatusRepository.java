package com.moviebooking.movie_booking.repository;

import com.moviebooking.movie_booking.entity.SeatStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatStatusRepository extends JpaRepository<SeatStatusEntity,Long> {
    SeatStatusEntity findBySeatIdAndShowtimeId(Long seatId,Long showtimeId);
}
