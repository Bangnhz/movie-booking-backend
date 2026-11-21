package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.converter.RoomConverter;
import com.moviebooking.movie_booking.entity.CinemaEntity;
import com.moviebooking.movie_booking.entity.RoomEntity;
import com.moviebooking.movie_booking.model.dto.RoomDTO;
import com.moviebooking.movie_booking.repository.*;
import com.moviebooking.movie_booking.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private CinemaRepository cinemaRepository;
    @Autowired
    private ShowtimeRepository showtimeRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private RoomConverter roomConverter;

    @Override
    public List<RoomDTO> getRoomsByCinemaId(Long cinemaId) {
        List<RoomEntity> roomEntities = roomRepository.findAllByCinemaId(cinemaId);
        return roomEntities.stream().map(roomEntity -> {
            RoomDTO dto = roomConverter.toRoomDTO(roomEntity);
            if (roomEntity.getCinema() != null) {
                dto.setCinemaId(roomEntity.getCinema().getId());
            }
            return dto;
        }).toList();
    }

    @Override
    @Transactional
    public RoomDTO createRoom(RoomDTO roomDTO) {
        if (roomDTO.getName() == null || roomDTO.getName().trim().isEmpty()) {
            throw new RuntimeException("Tên phòng chiếu không được để trống!");
        }
        if (roomDTO.getCinemaId() == null) {
            throw new RuntimeException("Cinema ID không được để trống!");
        }
        CinemaEntity cinema = cinemaRepository.findById(roomDTO.getCinemaId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy rạp chiếu với id=" + roomDTO.getCinemaId()));

        RoomEntity room = new RoomEntity();
        room.setName(roomDTO.getName().trim());
        room.setCinema(cinema);
        RoomEntity saved = roomRepository.save(room);

        RoomDTO result = roomConverter.toRoomDTO(saved);
        result.setDescription(roomDTO.getDescription());
        result.setCinemaId(cinema.getId());
        return result;
    }

    @Override
    @Transactional
    public RoomDTO updateRoom(Long roomId, RoomDTO roomDTO) {
        if (roomId == null) {
            throw new IllegalArgumentException("Room ID không được để trống!");
        }
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng chiếu với id=" + roomId));

        if (roomDTO.getName() != null && !roomDTO.getName().trim().isEmpty()) {
            room.setName(roomDTO.getName().trim());
        }
        if (roomDTO.getCinemaId() != null) {
            CinemaEntity cinema = cinemaRepository.findById(roomDTO.getCinemaId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy rạp chiếu với id=" + roomDTO.getCinemaId()));
            room.setCinema(cinema);
        }
        RoomEntity saved = roomRepository.save(room);

        RoomDTO result = roomConverter.toRoomDTO(saved);
        result.setDescription(roomDTO.getDescription());
        if (saved.getCinema() != null) {
            result.setCinemaId(saved.getCinema().getId());
        }
        return result;
    }

    @Override
    @Transactional
    public void deleteRoom(Long roomId) {
        if (roomId == null) {
            throw new IllegalArgumentException("Room ID không được để trống!");
        }
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng chiếu với id=" + roomId));

        if (showtimeRepository.existsByRoomId(roomId) || ticketRepository.existsByShowtime_Room_Id(roomId)) {
            throw new RuntimeException("Không thể xóa phòng chiếu vì đã có lịch chiếu hoặc vé liên quan!");
        }

        if (room.getSeats() != null && !room.getSeats().isEmpty()) {
            seatRepository.deleteAll(room.getSeats());
        }

        roomRepository.delete(room);
    }
}
