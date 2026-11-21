package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.converter.RoomConverter;
import com.moviebooking.movie_booking.entity.CinemaEntity;
import com.moviebooking.movie_booking.entity.RoomEntity;
import com.moviebooking.movie_booking.model.dto.RoomDTO;
import com.moviebooking.movie_booking.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTests {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private CinemaRepository cinemaRepository;

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private RoomConverter roomConverter;

    @InjectMocks
    private RoomServiceImpl roomService;

    private RoomEntity roomEntity;
    private CinemaEntity cinemaEntity;

    @BeforeEach
    void setUp() {
        cinemaEntity = new CinemaEntity();
        cinemaEntity.setId(10L);
        cinemaEntity.setName("CGV");

        roomEntity = new RoomEntity();
        roomEntity.setId(1L);
        roomEntity.setName("Phòng 1");
        roomEntity.setCinema(cinemaEntity);
    }

    @Test
    void createRoom_ThrowsException_WhenNameOrCinemaIdMissing() {
        RoomDTO dto = new RoomDTO();
        assertThrows(RuntimeException.class, () -> roomService.createRoom(dto));

        dto.setName("Phòng 2");
        assertThrows(RuntimeException.class, () -> roomService.createRoom(dto));
    }

    @Test
    void createRoom_Success() {
        RoomDTO dto = new RoomDTO();
        dto.setName("Phòng 1");
        dto.setCinemaId(10L);

        when(cinemaRepository.findById(10L)).thenReturn(Optional.of(cinemaEntity));
        when(roomRepository.save(any(RoomEntity.class))).thenReturn(roomEntity);
        when(roomConverter.toRoomDTO(roomEntity)).thenReturn(new RoomDTO(1L, "Phòng 1", null, 10L));

        RoomDTO created = roomService.createRoom(dto);

        assertNotNull(created);
        assertEquals(1L, created.getId());
        assertEquals("Phòng 1", created.getName());
    }

    @Test
    void updateRoom_Success() {
        RoomDTO dto = new RoomDTO();
        dto.setName("Phòng VIP");

        when(roomRepository.findById(1L)).thenReturn(Optional.of(roomEntity));
        when(roomRepository.save(roomEntity)).thenReturn(roomEntity);
        when(roomConverter.toRoomDTO(roomEntity)).thenReturn(new RoomDTO(1L, "Phòng VIP", null, 10L));

        RoomDTO updated = roomService.updateRoom(1L, dto);

        assertEquals("Phòng VIP", roomEntity.getName());
        verify(roomRepository, times(1)).save(roomEntity);
    }

    @Test
    void deleteRoom_ThrowsException_WhenShowtimesOrTicketsExist() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(roomEntity));
        when(showtimeRepository.existsByRoomId(1L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> roomService.deleteRoom(1L));
        assertTrue(ex.getMessage().contains("lịch chiếu"));
        verify(roomRepository, never()).delete(any());
    }

    @Test
    void deleteRoom_Success_WhenNoShowtimesOrTickets() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(roomEntity));
        when(showtimeRepository.existsByRoomId(1L)).thenReturn(false);
        when(ticketRepository.existsByShowtime_Room_Id(1L)).thenReturn(false);

        roomService.deleteRoom(1L);

        verify(roomRepository, times(1)).delete(roomEntity);
    }
}
