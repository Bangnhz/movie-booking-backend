package com.moviebooking.movie_booking.controller;

import com.moviebooking.movie_booking.model.request.BookingRequest;
import com.moviebooking.movie_booking.model.response.BookingResponse;
import com.moviebooking.movie_booking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest bookingRequest) {
        try{
            Long bookingId = bookingService.create(bookingRequest);
            return ResponseEntity.ok(bookingId);
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/hold")
    public ResponseEntity<?> holdBooking(@RequestBody BookingRequest bookingRequest) {
        try{
            String bookingId = bookingService.holdBooking(bookingRequest);
            return ResponseEntity.ok(bookingId);
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getHoldBooking(@PathVariable String bookingId){
        try {
            BookingResponse bookingResponse = bookingService.getTempBookingResponse(bookingId);
            return ResponseEntity.ok(bookingResponse);
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
//    @GetMapping("/{bookingId}")
//    public ResponseEntity<?> getBookings(@PathVariable Long bookingId){
//        try {
//            BookingResponse bookingResponseDTO = bookingService.getBookingResponse(bookingId);
//            return ResponseEntity.ok(bookingResponseDTO);
//        }
//        catch (Exception e){
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
}
