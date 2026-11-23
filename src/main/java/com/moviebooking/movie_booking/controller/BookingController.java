package com.moviebooking.movie_booking.controller;

import com.moviebooking.movie_booking.entity.BookingEntity;
import com.moviebooking.movie_booking.model.request.BookingRequest;
import com.moviebooking.movie_booking.model.response.BookingResponse;
import com.moviebooking.movie_booking.repository.BookingRepository;
import com.moviebooking.movie_booking.repository.UserRepository;
import com.moviebooking.movie_booking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingRepository bookingRepository;

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest bookingRequest,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try{
            Long bookingId = bookingService.create(bookingRequest, userDetails.getUsername());
            return ResponseEntity.ok(bookingId);
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/hold")
    public ResponseEntity<?> holdBooking(@RequestBody BookingRequest bookingRequest,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try{
            Long userId = getUserId(userDetails);
            String tempBookingId = bookingService.holdBooking(bookingRequest, userId);
            return ResponseEntity.ok(tempBookingId);
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getHoldBooking(@PathVariable String bookingId,
                                            @AuthenticationPrincipal UserDetails userDetails){
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            BookingResponse bookingResponse = bookingService.getTempBookingResponse(bookingId, getUserId(userDetails));
            return ResponseEntity.ok(bookingResponse);
        }
        catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/hold/{bookingId}")
    public ResponseEntity<?> cancelHoldBooking(@PathVariable String bookingId,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            bookingService.cancelTempBooking(bookingId, getUserId(userDetails));
            return ResponseEntity.ok(Map.of("message", "Hủy phiên giữ ghế thành công"));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> cancelBooking(@PathVariable String bookingId,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Long userId = getUserId(userDetails);
            try {
                Long dbBookingId = Long.parseLong(bookingId);
                Optional<BookingEntity> bookingOpt = bookingRepository.findById(dbBookingId);
                if (bookingOpt.isPresent()) {
                    BookingEntity b = bookingOpt.get();
                    if (!b.getUser().getId().equals(userId)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền hủy booking này");
                    }
                    bookingService.delete(dbBookingId);
                    return ResponseEntity.ok(Map.of("message", "Hủy booking thành công"));
                }
            } catch (NumberFormatException ignored) {
                // Không phải số -> tiếp tục xử lý như tempBookingId
            }

            bookingService.cancelTempBooking(bookingId, userId);
            return ResponseEntity.ok(Map.of("message", "Hủy phiên giữ ghế thành công"));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"))
                .getId();
    }
}
