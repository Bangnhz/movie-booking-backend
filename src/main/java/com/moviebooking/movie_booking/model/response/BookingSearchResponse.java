package com.moviebooking.movie_booking.model.response;

import com.moviebooking.movie_booking.enums.BookingStatus;
import com.moviebooking.movie_booking.model.dto.ShowtimeSeatDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSearchResponse {
    // 1. Thông tin Đơn hàng (Booking)
    private Integer bookingId;       // Khớp với b.id
    private LocalDateTime bookingDate; // Khớp với b.created_at
    private BookingStatus status;     // Khớp với b.status
    private BigDecimal totalAmount;   // Khớp với b.total_price

    // 2. Thông tin Phim (Movie)
    private Integer movieId;          // Khớp với m.id
    private String movieTitle;        // Khớp với m.title
    private String posterUrl;         // Khớp với m.poster_url
    private String ageRating;         // Khớp với m.age_rating

    // 3. Thông tin Rạp & Phòng (Cinema & Room)
    private Integer cinemaId;         // Khớp với c.id
    private String cinemaName;        // Khớp với c.name
    private Integer roomId;           // Khớp với r.id
    private String roomName;          // Khớp với r.name

    // 4. Thông tin Suất chiếu (Showtime)
    private Integer showtimeId;       // Khớp với st.id
    private LocalDateTime startTime;  // Khớp với st.start_time
    private LocalDateTime endTime;    // Khớp với st.end_time

    // 5. Chỗ ngồi (Ticket & Seat)
    private List<ShowtimeSeatDTO> seats;
}