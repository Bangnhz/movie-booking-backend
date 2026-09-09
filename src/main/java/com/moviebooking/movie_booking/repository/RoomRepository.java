package com.moviebooking.movie_booking.repository;

import com.moviebooking.movie_booking.entity.RoomEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<RoomEntity,Long> {
    List<RoomEntity> findAllByCinemaId(Long cinemaId);
}
