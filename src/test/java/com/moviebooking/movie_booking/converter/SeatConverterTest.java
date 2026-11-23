package com.moviebooking.movie_booking.converter;

import com.moviebooking.movie_booking.entity.SeatEntity;
import com.moviebooking.movie_booking.entity.SeatStatusEntity;
import com.moviebooking.movie_booking.entity.SeatTypeEntity;
import com.moviebooking.movie_booking.entity.ShowtimeEntity;
import com.moviebooking.movie_booking.enums.SeatStatus;
import com.moviebooking.movie_booking.model.dto.ShowtimeSeatDTO;
import com.moviebooking.movie_booking.service.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class SeatConverterTest {

    private RedisService redisService;
    private SeatConverter seatConverter;

    @BeforeEach
    void setUp() {
        redisService = Mockito.mock(RedisService.class);
        seatConverter = new SeatConverter(redisService);
    }

    private SeatEntity createSeat(Long seatId, String rowIndex, int columnIndex) {
        SeatEntity seat = new SeatEntity();
        seat.setId(seatId);
        seat.setRowIndex(rowIndex);
        seat.setColumnIndex(columnIndex);

        SeatTypeEntity type = new SeatTypeEntity();
        type.setName("STANDARD");
        type.setSurcharge(BigDecimal.ZERO);
        seat.setSeatType(type);
        return seat;
    }

    private ShowtimeEntity createShowtime(Long showtimeId) {
        ShowtimeEntity showtime = new ShowtimeEntity();
        showtime.setId(showtimeId);
        return showtime;
    }

    @Test
    @DisplayName("Ghế không có status trong DB và không giữ trên Redis -> AVAILABLE")
    void testToSeatDTO_NoStatusInDB_NotHeldInRedis() {
        Long showtimeId = 10L;
        SeatEntity seat = createSeat(1L, "A", 1);
        seat.setStatuses(Collections.emptyList());

        when(redisService.isSeatHeld(showtimeId, 1L)).thenReturn(false);

        ShowtimeSeatDTO dto = seatConverter.toSeatDTO(seat, showtimeId);

        assertEquals(SeatStatus.AVAILABLE, dto.getStatus());
    }

    @Test
    @DisplayName("Ghế không có status trong DB nhưng đang được giữ trên Redis -> HELD")
    void testToSeatDTO_NoStatusInDB_HeldInRedis() {
        Long showtimeId = 10L;
        SeatEntity seat = createSeat(1L, "A", 1);
        seat.setStatuses(Collections.emptyList());

        when(redisService.isSeatHeld(showtimeId, 1L)).thenReturn(true);

        ShowtimeSeatDTO dto = seatConverter.toSeatDTO(seat, showtimeId);

        assertEquals(SeatStatus.HELD, dto.getStatus());
    }

    @Test
    @DisplayName("Ghế có status HELD trong DB và holdExpiresAt còn hiệu lực -> HELD")
    void testToSeatDTO_HeldInDB_NotExpired() {
        Long showtimeId = 10L;
        SeatEntity seat = createSeat(1L, "A", 1);

        SeatStatusEntity status = new SeatStatusEntity();
        status.setSeat(seat);
        status.setShowtime(createShowtime(showtimeId));
        status.setStatus(SeatStatus.HELD);
        status.setHoldExpiresAt(LocalDateTime.now().plusMinutes(10));
        seat.setStatuses(List.of(status));

        ShowtimeSeatDTO dto = seatConverter.toSeatDTO(seat, showtimeId);

        assertEquals(SeatStatus.HELD, dto.getStatus());
    }

    @Test
    @DisplayName("Ghế có status HELD trong DB nhưng holdExpiresAt đã quá hạn, không giữ trên Redis -> AVAILABLE")
    void testToSeatDTO_HeldInDB_Expired_NotHeldInRedis() {
        Long showtimeId = 10L;
        SeatEntity seat = createSeat(1L, "A", 1);

        SeatStatusEntity status = new SeatStatusEntity();
        status.setSeat(seat);
        status.setShowtime(createShowtime(showtimeId));
        status.setStatus(SeatStatus.HELD);
        status.setHoldExpiresAt(LocalDateTime.now().minusMinutes(2));
        seat.setStatuses(List.of(status));

        when(redisService.isSeatHeld(showtimeId, 1L)).thenReturn(false);

        ShowtimeSeatDTO dto = seatConverter.toSeatDTO(seat, showtimeId);

        assertEquals(SeatStatus.AVAILABLE, dto.getStatus());
    }

    @Test
    @DisplayName("Ghế có status HELD trong DB nhưng holdExpiresAt đã quá hạn, nhưng Redis vẫn giữ -> HELD")
    void testToSeatDTO_HeldInDB_Expired_HeldInRedis() {
        Long showtimeId = 10L;
        SeatEntity seat = createSeat(1L, "A", 1);

        SeatStatusEntity status = new SeatStatusEntity();
        status.setSeat(seat);
        status.setShowtime(createShowtime(showtimeId));
        status.setStatus(SeatStatus.HELD);
        status.setHoldExpiresAt(LocalDateTime.now().minusMinutes(2));
        seat.setStatuses(List.of(status));

        when(redisService.isSeatHeld(showtimeId, 1L)).thenReturn(true);

        ShowtimeSeatDTO dto = seatConverter.toSeatDTO(seat, showtimeId);

        assertEquals(SeatStatus.HELD, dto.getStatus());
    }

    @Test
    @DisplayName("Ghế có status BOOKED trong DB -> luôn là BOOKED")
    void testToSeatDTO_BookedInDB() {
        Long showtimeId = 10L;
        SeatEntity seat = createSeat(1L, "A", 1);

        SeatStatusEntity status = new SeatStatusEntity();
        status.setSeat(seat);
        status.setShowtime(createShowtime(showtimeId));
        status.setStatus(SeatStatus.BOOKED);
        seat.setStatuses(List.of(status));

        ShowtimeSeatDTO dto = seatConverter.toSeatDTO(seat, showtimeId);

        assertEquals(SeatStatus.BOOKED, dto.getStatus());
    }

    @Test
    @DisplayName("Dùng batch Set<Long> heldSeatIds: Ghế nằm trong tập hợp held -> HELD")
    void testToSeatDTO_WithBatchHeldSeatIds() {
        Long showtimeId = 10L;
        SeatEntity seat1 = createSeat(1L, "A", 1);
        SeatEntity seat2 = createSeat(2L, "A", 2);
        seat1.setStatuses(Collections.emptyList());
        seat2.setStatuses(Collections.emptyList());

        Set<Long> heldIds = Set.of(1L);

        ShowtimeSeatDTO dto1 = seatConverter.toSeatDTO(seat1, showtimeId, heldIds);
        ShowtimeSeatDTO dto2 = seatConverter.toSeatDTO(seat2, showtimeId, heldIds);

        assertEquals(SeatStatus.HELD, dto1.getStatus());
        assertEquals(SeatStatus.AVAILABLE, dto2.getStatus());
    }
}
