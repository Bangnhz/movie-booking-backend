package com.moviebooking.movie_booking.controller;

import com.moviebooking.movie_booking.model.dto.RoomSeatDTO;
import com.moviebooking.movie_booking.repository.SeatRepository;
import com.moviebooking.movie_booking.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seats")
public class SeatController {
    @Autowired
    private SeatService seatService;

    @GetMapping("/rooms/{roomId}")
    private ResponseEntity<?> getSeatsByRoom(@PathVariable Long roomId){
        try{
            List<RoomSeatDTO> seats = seatService.getSeatsByRoomId(roomId);
            return ResponseEntity.ok(seats);
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(Map.of("error", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }
}
