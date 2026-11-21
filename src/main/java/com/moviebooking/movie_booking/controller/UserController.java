package com.moviebooking.movie_booking.controller;


import com.moviebooking.movie_booking.entity.UserEntity;
import com.moviebooking.movie_booking.entity.UserRoleEntity;
import com.moviebooking.movie_booking.model.dto.RoleDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Vui lòng đăng nhập để xem thông tin hồ sơ"));
        }
        UserEntity user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Không tìm thấy thông tin tài khoản đang đăng nhập"));
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setFullname(user.getFullname());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhone(user.getPhone());
        userDTO.setPoints(user.getPoints());

        java.util.List<RoleDTO> roleDTOs = new java.util.ArrayList<>();
        if (user.getUserRoles() != null) {
            for (UserRoleEntity ur : user.getUserRoles()) {
                if (ur.getRole() != null) {
                    roleDTOs.add(new RoleDTO(ur.getRole().getId(), ur.getRole().getName()));
                }
            }
        }
        userDTO.setRoles(roleDTOs);
        if (!roleDTOs.isEmpty()) {
            userDTO.setRole(roleDTOs.get(0).getName());
        }

        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/me/bookings")
    public ResponseEntity<?> getMyBookings(
            @ModelAttribute BookingSearchRequest bookingSearchRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Vui lòng đăng nhập để xem lịch sử đặt vé"));
        }
        UserEntity currentUser = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Không tìm thấy thông tin tài khoản đang đăng nhập"));
        }
        return getBookingsByUserId(currentUser.getId(), bookingSearchRequest, page, size, sort, userDetails);
    }

    @GetMapping
    public ResponseEntity<Page<UserDTO>> getUsers(
            @ModelAttribute UserSearchRequest userSearchRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort
            ) {
        String sortField = "id";
        Sort.Direction direction = Sort.Direction.ASC;
        if (sort != null && sort.length > 0) {
            String[] parts = sort[0].contains(",") ? sort[0].split(",") : sort;
            if (parts.length > 0 && !parts[0].isBlank()) sortField = parts[0];
            if (parts.length > 1 && !parts[1].isBlank()) {
                direction = Sort.Direction.fromOptionalString(parts[1]).orElse(Sort.Direction.ASC);
            }
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<UserDTO> userDTOPage = userService.findAll(userSearchRequest, pageable);
        return ResponseEntity.ok(userDTOPage);
    }
    @GetMapping("/{userId}/bookings")
    public ResponseEntity<?> getBookingsByUserId(
            @PathVariable Long userId,
            @ModelAttribute BookingSearchRequest bookingSearchRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort,
            @AuthenticationPrincipal UserDetails userDetails
            ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Vui lòng đăng nhập để xem lịch sử đặt vé"));
        }

        UserEntity currentUser = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Không tìm thấy thông tin tài khoản đang đăng nhập"));
        }

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()) || "ADMIN".equalsIgnoreCase(a.getAuthority()));

        if (!isAdmin && !currentUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Bạn không có quyền xem lịch sử đặt vé của người dùng khác"));
        }

        String sortField = "id";
        Sort.Direction direction = Sort.Direction.ASC;
        if (sort != null && sort.length > 0) {
            String[] parts = sort[0].contains(",") ? sort[0].split(",") : sort;
            if (parts.length > 0 && !parts[0].isBlank()) sortField = parts[0];
            if (parts.length > 1 && !parts[1].isBlank()) {
                direction = Sort.Direction.fromOptionalString(parts[1]).orElse(Sort.Direction.ASC);
            }
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<BookingSearchResponse> bookingSearchResponses = bookingService.getBookingsByUserId(userId, bookingSearchRequest, pageable);
        return ResponseEntity.ok(bookingSearchResponses);
    }
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.delete(userId);
            return ResponseEntity.noContent().build();
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }
    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody UserDTO userDTO) {
        try {
            UserDTO updated = userService.update(userDTO);
            return ResponseEntity.ok(updated);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }
}
