package com.moviebooking.movie_booking.repository;

import com.moviebooking.movie_booking.entity.BookingEntity;
import com.moviebooking.movie_booking.enums.BookingStatus;
import com.moviebooking.movie_booking.repository.customer.BookingRepositoryCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<BookingEntity,Long>, BookingRepositoryCustomer {
    List<BookingEntity> findAllByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime createdAt);
    List<BookingEntity> findAllByUserIdAndShowtimeIdAndStatus(Long userId,Long showtimeId,BookingStatus status);
}
