package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.entity.*;
import com.moviebooking.movie_booking.enums.BookingStatus;
import com.moviebooking.movie_booking.enums.SeatStatus;
import com.moviebooking.movie_booking.model.TempBookingRequest;
import com.moviebooking.movie_booking.model.request.BookingRequest;
import com.moviebooking.movie_booking.repository.*;
import com.moviebooking.movie_booking.service.RedisService;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTests {
    @Mock private BookingRepository bookingRepository;
    @Mock private ShowtimeRepository showtimeRepository;
    @Mock private UserRepository userRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private TicketRepository ticketRepository;
    @Mock private SeatStatusRepository seatStatusRepository;
    @Mock private RedisService redisService;
    @Mock private EntityManager entityManager;
    @InjectMocks private BookingServiceImpl service;

    private BookingRequest request;
    private ShowtimeEntity showtime;
    private SeatEntity seat;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        request = new BookingRequest();
        request.setShowtimeId(2L);
        request.setSeatIds(List.of(3L));
        RoomEntity room = new RoomEntity();
        room.setId(4L);
        showtime = new ShowtimeEntity();
        showtime.setId(2L);
        showtime.setRoom(room);
        showtime.setStartTime(LocalDateTime.now().plusDays(1));
        showtime.setBasePrice(new BigDecimal("80000"));
        SeatTypeEntity type = new SeatTypeEntity();
        type.setSurcharge(new BigDecimal("20000"));
        seat = new SeatEntity();
        seat.setId(3L);
        seat.setRoom(room);
        seat.setSeatType(type);
        user = new UserEntity();
        user.setId(1L);
    }

    private void stubBooking() {
        when(showtimeRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(showtime));
        when(userRepository.findByUsername("test-user")).thenReturn(Optional.of(user));
        when(seatRepository.findById(3L)).thenReturn(Optional.of(seat));
    }

    @Test
    void createsPendingBookingWithTicketsAndServerCalculatedPrice() {
        stubBooking();
        when(redisService.reserveSeats(2L, List.of(3L), 1L, 15)).thenReturn(true);
        when(bookingRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            BookingEntity booking = invocation.getArgument(0);
            assertEquals(BookingStatus.PENDING, booking.getStatus());
            assertEquals(new BigDecimal("100000"), booking.getTotalPrice());
            assertEquals(1, booking.getTickets().size());
            TicketEntity ticket = booking.getTickets().getFirst();
            assertSame(booking, ticket.getBooking());
            assertSame(seat, ticket.getSeat());
            assertEquals(BigDecimal.ZERO, ticket.getDiscountAmount());
            assertEquals(booking.getTotalPrice(), ticket.getFinalPrice());
            booking.setId(10L);
            return booking;
        });
        assertEquals(10L, service.create(request, "test-user"));
        verify(seatStatusRepository).save(argThat(status -> status.getStatus() == SeatStatus.HELD
                && status.getHoldExpiresAt().isAfter(LocalDateTime.now().plusMinutes(14))));
    }

    @Test
    void rejectsDuplicateSeatIdsBeforeAccessingStorage() {
        request.setSeatIds(List.of(3L, 3L));
        assertThrows(IllegalArgumentException.class, () -> service.create(request, "test-user"));
        verifyNoInteractions(showtimeRepository, redisService, bookingRepository);
    }

    @Test
    void rejectsSeatsFromAnotherRoom() {
        stubBooking();
        RoomEntity otherRoom = new RoomEntity();
        otherRoom.setId(99L);
        seat.setRoom(otherRoom);
        assertThrows(IllegalArgumentException.class, () -> service.create(request, "test-user"));
        verifyNoInteractions(redisService, bookingRepository);
    }

    @Test
    void rejectsAlreadyAllocatedSeat() {
        stubBooking();
        when(ticketRepository.existsByShowtimeIdAndSeatIdAndBooking_StatusIn(eq(2L), eq(3L), any()))
                .thenReturn(true);
        assertThrows(IllegalStateException.class, () -> service.create(request, "test-user"));
        verifyNoInteractions(redisService, bookingRepository);
    }

    @Test
    void doesNotPersistWhenRedisHoldConflicts() {
        stubBooking();
        assertThrows(IllegalStateException.class, () -> service.create(request, "test-user"));
        verify(bookingRepository, never()).saveAndFlush(any());
        verify(seatStatusRepository, never()).save(any());
    }

    @Test
    void rejectsShowtimeThatAlreadyStarted() {
        showtime.setStartTime(LocalDateTime.now().minusMinutes(1));
        when(showtimeRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(showtime));
        assertThrows(IllegalStateException.class, () -> service.create(request, "test-user"));
        verifyNoInteractions(redisService, bookingRepository);
    }

    @Test
    void storesUserIdWithTemporaryBooking() {
        when(showtimeRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(showtime));
        when(userRepository.existsById(1L)).thenReturn(true);
        when(seatRepository.findById(3L)).thenReturn(Optional.of(seat));
        when(redisService.holdSeats(2L, List.of(3L), 1L, 15)).thenReturn(true);
        when(redisService.saveTempBooking(request, 1L, 15)).thenReturn("temp-booking-id");

        assertEquals("temp-booking-id", service.holdBooking(request, 1L));

        verify(redisService).saveTempBooking(request, 1L, 15);
    }

    @Test
    void releasesOwnHoldWhenSavingTemporaryBookingFails() {
        when(showtimeRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(showtime));
        when(userRepository.existsById(1L)).thenReturn(true);
        when(seatRepository.findById(3L)).thenReturn(Optional.of(seat));
        when(redisService.holdSeats(2L, List.of(3L), 1L, 15)).thenReturn(true);
        when(redisService.saveTempBooking(request, 1L, 15)).thenThrow(new IllegalStateException("Redis failed"));
        assertThrows(IllegalStateException.class, () -> service.holdBooking(request, 1L));
        verify(redisService).releaseSeats(2L, List.of(3L), 1L);
    }

    @Test
    void rejectsTemporaryBookingOwnedByAnotherUser() {
        TempBookingRequest tempBooking = new TempBookingRequest();
        tempBooking.setUserId(2L);
        tempBooking.setBookingRequest(request);
        when(redisService.getTempBooking("temp-booking-id")).thenReturn(tempBooking);

        assertThrows(AccessDeniedException.class,
                () -> service.getTempBookingResponse("temp-booking-id", 1L));

        verifyNoInteractions(showtimeRepository, seatRepository);
    }

    @Test
    void releasesReservationAfterDatabaseRollback() {
        stubBooking();
        when(redisService.reserveSeats(2L, List.of(3L), 1L, 15)).thenReturn(true);
        when(bookingRepository.saveAndFlush(any())).thenThrow(new IllegalStateException("Database failed"));
        TransactionSynchronizationManager.initSynchronization();
        try {
            assertThrows(IllegalStateException.class, () -> service.create(request, "test-user"));
            verify(redisService, never()).releaseSeats(anyLong(), anyList(), anyLong());
            TransactionSynchronizationManager.getSynchronizations().forEach(sync ->
                    sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
            verify(redisService).releaseSeats(2L, List.of(3L), 1L);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void cancellationDoesNotDeleteUserAndReleasesHoldOnlyAfterCommit() {
        BookingEntity booking = new BookingEntity();
        booking.setId(10L);
        booking.setUser(user);
        booking.setShowtime(showtime);
        booking.setStatus(BookingStatus.PENDING);
        TicketEntity ticket = new TicketEntity();
        ticket.setSeat(seat);
        booking.setTickets(new java.util.ArrayList<>(List.of(ticket)));
        SeatStatusEntity status = new SeatStatusEntity();
        status.setStatus(SeatStatus.HELD);
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(showtimeRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(showtime));
        when(seatStatusRepository.findBySeatIdAndShowtimeId(3L, 2L)).thenReturn(status);
        TransactionSynchronizationManager.initSynchronization();
        try {
            service.delete(10L);
            assertEquals(BookingStatus.CANCELLED, booking.getStatus());
            assertEquals(SeatStatus.AVAILABLE, status.getStatus());
            assertEquals(List.of(ticket), booking.getTickets());
            verifyNoInteractions(userRepository, ticketRepository);
            verify(redisService, never()).releaseSeats(anyLong(), anyList(), anyLong());
            TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
            verify(redisService).releaseSeats(2L, List.of(3L), 1L);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}