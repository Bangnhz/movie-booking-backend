package com.moviebooking.movie_booking.repository;

import com.moviebooking.movie_booking.entity.PaymentEntity;
import com.moviebooking.movie_booking.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository
        extends JpaRepository<PaymentEntity, Long> {

    List<PaymentEntity> findAllByBooking_Id(Long bookingId);
    Optional<PaymentEntity> findTopByBooking_IdOrderByCreatedAtDesc(Long bookingId);
    Optional<PaymentEntity> findTopByBooking_IdAndStatusOrderByCreatedAtDesc(Long bookingId, PaymentStatus status);
    Optional<PaymentEntity> findByTransactionCode(String transactionCode);
}
