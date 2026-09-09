package com.moviebooking.movie_booking.model.response;

import com.moviebooking.movie_booking.enums.BookingStatus;
import com.moviebooking.movie_booking.model.dto.MovieBookingDTO;
import com.moviebooking.movie_booking.model.dto.ShowtimeSeatDTO;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class BookingResponse {
    // Thông tin định danh đơn hàng
    private String id;
    private BookingStatus status;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime expirationTime;


    private MovieBookingDTO movieBookingDTO;


    // Nhóm thông tin Suất chiếu & Rạp
    private String cinemaName;
    private String roomName;
    private LocalDateTime startTime;

    // Nhóm thông tin Ghế (Danh sách Object để TicketCard map được)
    private List<ShowtimeSeatDTO> selectedSeats;
}
