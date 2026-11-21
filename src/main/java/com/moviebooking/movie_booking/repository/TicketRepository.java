package com.moviebooking.movie_booking.repository;

import com.moviebooking.movie_booking.entity.TicketEntity;
import com.moviebooking.movie_booking.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<TicketEntity, Long> {
    boolean existsByShowtime_Room_Id(Long roomId);
    boolean existsByShowtimeIdAndSeatIdAndBooking_StatusIn(Long showtimeId, Long seatId, BookingStatus[] bookingStatus);
    boolean existsByShowtimeIdAndSeatIdAndBooking_StatusInAndBooking_IdNot(Long showtimeId, Long seatId, BookingStatus[] bookingStatus, Long bookingId);
}
