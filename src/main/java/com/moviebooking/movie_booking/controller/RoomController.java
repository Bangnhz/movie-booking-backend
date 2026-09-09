package com.moviebooking.movie_booking.controller;

import com.moviebooking.movie_booking.model.dto.RoomDTO;
import com.moviebooking.movie_booking.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    @Autowired
    private RoomService roomService;
    @GetMapping("/cinemas/{cinemaId}")
    private ResponseEntity<?> getCinemas(@PathVariable Long cinemaId) {
        try {
            List<RoomDTO> roomDTOs = roomService.getRoomsByCinemaId(cinemaId);
            return ResponseEntity.ok(roomDTOs);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }
}
