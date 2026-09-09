package com.moviebooking.movie_booking.repository;

import com.moviebooking.movie_booking.entity.MovieEntity;
import com.moviebooking.movie_booking.entity.ShowtimeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowtimeRepository extends JpaRepository<ShowtimeEntity,Long> {
//    @Query("SELECT m FROM ShowtimeEntity s " +
//            "JOIN FETCH s.movie m " +
//            "WHERE s.room.cinema.id = :cinemaId " +
//            "AND s.startTime >= CURRENT_TIMESTAMP " +
//            "ORDER BY m.id, s.startTime")
//    List<ShowtimeEntity> findByCinema(@Param("cinemaId") Long cinemaId);

    @Query("SELECT DISTINCT m FROM MovieEntity m " +
            "JOIN FETCH m.showtimes s " +
            "WHERE s.room.cinema.id = :cinemaId " +
            "AND s.startTime >= :start " +
            "ORDER BY m.title ASC, s.startTime ASC")
    List<MovieEntity> findMoviesWithShowtimes(
            @Param("cinemaId") Long cinemaId,
            @Param("start") LocalDateTime start
    );
}
