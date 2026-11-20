package com.moviebooking.movie_booking.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "tickets")
@Getter
@Setter
public class TicketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private BookingEntity booking;

    @ManyToOne
    @JoinColumn(name = "showtime_id", nullable = false)
    private ShowtimeEntity showtime;

    @ManyToOne
    @JoinColumn(name = "seat_id", nullable = false)
    private SeatEntity seat;

    private BigDecimal originalPrice;

    private BigDecimal discountAmount;

    private BigDecimal finalPrice;
}