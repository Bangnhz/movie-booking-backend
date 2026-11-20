package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.entity.*;
import com.moviebooking.movie_booking.enums.BookingStatus;
import com.moviebooking.movie_booking.enums.PaymentStatus;
import com.moviebooking.movie_booking.enums.SeatStatus;
import com.moviebooking.movie_booking.model.dto.VNPayQueryResult;
import com.moviebooking.movie_booking.repository.*;
import com.moviebooking.movie_booking.service.BookingService;
import com.moviebooking.movie_booking.service.RedisService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingCleanupServiceImplTests {
    @Mock private BookingRepository bookingRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private SeatStatusRepository seatStatusRepository;
    @Mock private ShowtimeRepository showtimeRepository;
    @Mock private BookingService bookingService;
    @Mock private VNPayService vnPayService;
    @Mock private RedisService redisService;
    @Mock private EntityManager entityManager;
    @InjectMocks private BookingCleanupServiceImpl service;

    private BookingEntity expiredBooking(int minutesAgo) {
        ShowtimeEntity showtime = new ShowtimeEntity();
        showtime.setId(2L);
        SeatEntity seat = new SeatEntity();
        seat.setId(3L);
        BookingEntity booking = new BookingEntity();
        booking.setId(10L);
        booking.setShowtime(showtime);
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now().minusMinutes(minutesAgo));
        TicketEntity ticket = new TicketEntity();
        ticket.setBooking(booking);
        ticket.setSeat(seat);
        ticket.setShowtime(showtime);
        booking.setTickets(new ArrayList<>(List.of(ticket)));
        when(bookingRepository.findAllByStatusAndCreatedAtBefore(eq(BookingStatus.PENDING), any()))
                .thenReturn(new ArrayList<>(List.of(booking)));
        when(showtimeRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(showtime));
        return booking;
    }

    @Test
    void cancelsExpiredBookingAndPreservesTickets_WhenNoPendingPayment() {
        BookingEntity booking = expiredBooking(16);
        TicketEntity originalTicket = booking.getTickets().getFirst();
        SeatStatusEntity status = new SeatStatusEntity();
        status.setStatus(SeatStatus.HELD);
        status.setHoldExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(seatStatusRepository.findBySeatIdAndShowtimeId(3L, 2L)).thenReturn(status);
        when(paymentRepository.findTopByBooking_IdAndStatusOrderByCreatedAtDesc(10L, PaymentStatus.PENDING))
                .thenReturn(Optional.empty());

        service.cleanupExpiredBookings();

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(SeatStatus.AVAILABLE, status.getStatus());
        assertNull(status.getHoldExpiresAt());
        assertEquals(List.of(originalTicket), booking.getTickets());
        assertSame(booking, originalTicket.getBooking());
        verify(bookingRepository).findAllByStatusAndCreatedAtBefore(eq(BookingStatus.PENDING),
                argThat(cutoff -> cutoff.isBefore(LocalDateTime.now().minusMinutes(14))
                        && cutoff.isAfter(LocalDateTime.now().minusMinutes(16))));
    }

    @Test
    void holdsSeatsWhenPaymentIsPendingAndQueryPending() {
        BookingEntity booking = expiredBooking(16);
        PaymentEntity pendingPayment = new PaymentEntity();
        pendingPayment.setTransactionCode("TXN-12345");
        pendingPayment.setStatus(PaymentStatus.PENDING);
        pendingPayment.setCreatedAt(LocalDateTime.now().minusMinutes(5));

        when(paymentRepository.findTopByBooking_IdAndStatusOrderByCreatedAtDesc(10L, PaymentStatus.PENDING))
                .thenReturn(Optional.of(pendingPayment));
        when(vnPayService.queryTransaction(eq("TXN-12345"), any()))
                .thenReturn(VNPayQueryResult.builder().pending(true).build());

        service.cleanupExpiredBookings();

        // Ghế KHÔNG bị hủy, booking vẫn là PENDING
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        verifyNoInteractions(seatStatusRepository);
        verify(bookingRepository, never()).save(any());
        verify(bookingService, never()).processPaymentResult(anyString(), anyBoolean());
    }

    @Test
    void confirmsBookingWhenQueryReturnsSuccess() {
        BookingEntity booking = expiredBooking(16);
        PaymentEntity pendingPayment = new PaymentEntity();
        pendingPayment.setTransactionCode("TXN-SUCCESS");
        pendingPayment.setStatus(PaymentStatus.PENDING);
        pendingPayment.setCreatedAt(LocalDateTime.now().minusMinutes(5));

        when(paymentRepository.findTopByBooking_IdAndStatusOrderByCreatedAtDesc(10L, PaymentStatus.PENDING))
                .thenReturn(Optional.of(pendingPayment));
        when(vnPayService.queryTransaction(eq("TXN-SUCCESS"), any()))
                .thenReturn(VNPayQueryResult.builder().success(true).build());

        service.cleanupExpiredBookings();

        verify(bookingService).processPaymentResult("TXN-SUCCESS", true);
        verifyNoInteractions(seatStatusRepository);
    }

    @Test
    void cancelsBookingWhenQueryReturnsFailed() {
        BookingEntity booking = expiredBooking(16);
        PaymentEntity pendingPayment = new PaymentEntity();
        pendingPayment.setTransactionCode("TXN-FAIL");
        pendingPayment.setStatus(PaymentStatus.PENDING);
        pendingPayment.setCreatedAt(LocalDateTime.now().minusMinutes(5));

        SeatStatusEntity status = new SeatStatusEntity();
        status.setStatus(SeatStatus.HELD);
        when(seatStatusRepository.findBySeatIdAndShowtimeId(3L, 2L)).thenReturn(status);

        when(paymentRepository.findTopByBooking_IdAndStatusOrderByCreatedAtDesc(10L, PaymentStatus.PENDING))
                .thenReturn(Optional.of(pendingPayment));
        when(vnPayService.queryTransaction(eq("TXN-FAIL"), any()))
                .thenReturn(VNPayQueryResult.builder().failed(true).build());

        service.cleanupExpiredBookings();

        verify(bookingService).processPaymentResult("TXN-FAIL", false);
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(SeatStatus.AVAILABLE, status.getStatus());
    }

    @Test
    void cancelsBookingWhenPendingExceedsMaxGraceLimit() {
        // Đơn đã tạo cách đây 21 phút (quá giới hạn 20 phút)
        BookingEntity booking = expiredBooking(21);
        PaymentEntity pendingPayment = new PaymentEntity();
        pendingPayment.setTransactionCode("TXN-TIMEOUT");
        pendingPayment.setStatus(PaymentStatus.PENDING);
        pendingPayment.setCreatedAt(LocalDateTime.now().minusMinutes(10));

        SeatStatusEntity status = new SeatStatusEntity();
        status.setStatus(SeatStatus.HELD);
        when(seatStatusRepository.findBySeatIdAndShowtimeId(3L, 2L)).thenReturn(status);

        when(paymentRepository.findTopByBooking_IdAndStatusOrderByCreatedAtDesc(10L, PaymentStatus.PENDING))
                .thenReturn(Optional.of(pendingPayment));
        when(vnPayService.queryTransaction(eq("TXN-TIMEOUT"), any()))
                .thenReturn(VNPayQueryResult.builder().pending(true).build());

        service.cleanupExpiredBookings();

        verify(bookingService).processPaymentResult("TXN-TIMEOUT", false);
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(SeatStatus.AVAILABLE, status.getStatus());
    }

    @Test
    void skipsBookingPaidWhileWaitingForLock() {
        BookingEntity booking = expiredBooking(16);
        doAnswer(invocation -> {
            booking.setStatus(BookingStatus.PAID);
            return null;
        }).when(entityManager).refresh(booking);

        service.cleanupExpiredBookings();

        assertEquals(BookingStatus.PAID, booking.getStatus());
        verifyNoInteractions(paymentRepository);
        verifyNoInteractions(seatStatusRepository);
        verify(bookingRepository, never()).save(any());
    }
}
