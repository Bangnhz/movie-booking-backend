package com.moviebooking.movie_booking.controller;

import com.moviebooking.movie_booking.model.dto.RoomDTO;
import com.moviebooking.movie_booking.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    @Autowired
    private RoomService roomService;

    @GetMapping("/cinemas/{cinemaId}")
    public ResponseEntity<?> getCinemas(@PathVariable Long cinemaId) {
        try {
            List<RoomDTO> roomDTOs = roomService.getRoomsByCinemaId(cinemaId);
            return ResponseEntity.ok(roomDTOs);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody RoomDTO roomDTO) {
        try {
            RoomDTO created = roomService.createRoom(roomDTO);
            return ResponseEntity.ok(created);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<?> updateRoom(@PathVariable Long roomId, @RequestBody RoomDTO roomDTO) {
        try {
            RoomDTO updated = roomService.updateRoom(roomId, roomDTO);
            return ResponseEntity.ok(updated);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long roomId) {
        try {
            roomService.deleteRoom(roomId);
            return ResponseEntity.ok(Map.of("message", "Xóa phòng thành công"));
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }
}
