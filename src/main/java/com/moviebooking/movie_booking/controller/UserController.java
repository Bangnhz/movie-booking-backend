package com.moviebooking.movie_booking.controller;


import com.moviebooking.movie_booking.model.dto.UserDTO;
import com.moviebooking.movie_booking.model.request.search.BookingSearchRequest;
import com.moviebooking.movie_booking.model.request.search.UserSearchRequest;
import com.moviebooking.movie_booking.model.response.BookingSearchResponse;
import com.moviebooking.movie_booking.repository.UserRepository;
import com.moviebooking.movie_booking.service.BookingService;
import com.moviebooking.movie_booking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    private ResponseEntity<Page<UserDTO>> getUsers(
            @ModelAttribute UserSearchRequest userSearchRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort
            ) {
        Sort.Direction direction = Sort.Direction.fromString(sort[1]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction,sort[0]));
        Page<UserDTO> userDTOPage = userService.findAll(userSearchRequest, pageable);
        return ResponseEntity.ok(userDTOPage);
    }
    @GetMapping("/{userId}/bookings")
    private ResponseEntity<?> getBookingsByUserId(
            @PathVariable Long  userId,
            @ModelAttribute BookingSearchRequest bookingSearchRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort
            ) {
        Sort.Direction direction = Sort.Direction.fromString(sort[1]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction,sort[0]));
        Page<BookingSearchResponse> bookingSearchResponses = bookingService.getBookingsByUserId(userId,bookingSearchRequest,pageable);
        return ResponseEntity.ok(bookingSearchResponses);
    }
    @DeleteMapping("/{userId}")
    private ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.delete(userId);
            return ResponseEntity.noContent().build();
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }
    @PutMapping
    private ResponseEntity<?> updateUser(@RequestBody UserDTO userDTO) {
        try {
            userService.delete(userDTO.getId());
            return ResponseEntity.ok(userDTO);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }
}
