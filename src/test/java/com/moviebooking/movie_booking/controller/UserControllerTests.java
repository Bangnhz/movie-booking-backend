package com.moviebooking.movie_booking.controller;

import com.moviebooking.movie_booking.entity.UserEntity;
import com.moviebooking.movie_booking.model.dto.UserDTO;
import com.moviebooking.movie_booking.model.request.search.BookingSearchRequest;
import com.moviebooking.movie_booking.model.response.BookingSearchResponse;
import com.moviebooking.movie_booking.repository.UserRepository;
import com.moviebooking.movie_booking.service.BookingService;
import com.moviebooking.movie_booking.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTests {

    @Mock
    private UserService userService;

    @Mock
    private BookingService bookingService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController userController;

    private UserEntity normalUserEntity;
    private UserDetails normalUserDetails;
    private UserDetails adminUserDetails;

    @BeforeEach
    void setUp() {
        normalUserEntity = new UserEntity();
        normalUserEntity.setId(10L);
        normalUserEntity.setUsername("normalUser");

        normalUserDetails = User.withUsername("normalUser")
                .password("pass")
                .roles("USER")
                .build();

        adminUserDetails = User.withUsername("adminUser")
                .password("pass")
                .roles("ADMIN")
                .build();
    }

    @Test
    void getCurrentUser_ReturnsUnauthorized_WhenNoUserDetails() {
        ResponseEntity<?> response = userController.getCurrentUser(null);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getCurrentUser_ReturnsUnauthorized_WhenUserNotFoundInDb() {
        when(userRepository.findByUsername("normalUser")).thenReturn(Optional.empty());
        ResponseEntity<?> response = userController.getCurrentUser(normalUserDetails);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getCurrentUser_ReturnsProfile_WhenUserFound() {
        when(userRepository.findByUsername("normalUser")).thenReturn(Optional.of(normalUserEntity));
        ResponseEntity<?> response = userController.getCurrentUser(normalUserDetails);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserDTO dto = (UserDTO) response.getBody();
        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("normalUser", dto.getUsername());
    }

    @Test
    void getBookingsByUserId_ReturnsUnauthorized_WhenNoAuthentication() {
        ResponseEntity<?> response = userController.getBookingsByUserId(
                10L,
                new BookingSearchRequest(),
                0,
                10,
                new String[]{"id", "asc"},
                null
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("error"));
    }

    @Test
    void getBookingsByUserId_ReturnsUnauthorized_WhenUserNotFoundInDb() {
        when(userRepository.findByUsername("normalUser")).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.getBookingsByUserId(
                10L,
                new BookingSearchRequest(),
                0,
                10,
                new String[]{"id", "asc"},
                normalUserDetails
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getBookingsByUserId_ReturnsForbidden_WhenAccessingOtherUserAndNotAdmin() {
        when(userRepository.findByUsername("normalUser")).thenReturn(Optional.of(normalUserEntity));

        // User ID is 10, but requesting for userId 20
        ResponseEntity<?> response = userController.getBookingsByUserId(
                20L,
                new BookingSearchRequest(),
                0,
                10,
                new String[]{"id", "asc"},
                normalUserDetails
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("error"));
        verify(bookingService, never()).getBookingsByUserId(any(), any(), any());
    }

    @Test
    void getBookingsByUserId_Success_WhenUserIsOwner() {
        when(userRepository.findByUsername("normalUser")).thenReturn(Optional.of(normalUserEntity));
        Page<BookingSearchResponse> emptyPage = new PageImpl<>(Collections.emptyList());
        when(bookingService.getBookingsByUserId(eq(10L), any(BookingSearchRequest.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        ResponseEntity<?> response = userController.getBookingsByUserId(
                10L,
                new BookingSearchRequest(),
                0,
                10,
                new String[]{"id,asc"},
                normalUserDetails
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(emptyPage, response.getBody());
        verify(bookingService, times(1)).getBookingsByUserId(eq(10L), any(), any());
    }

    @Test
    void getBookingsByUserId_Success_WhenUserIsAdminViewingOtherUser() {
        UserEntity adminEntity = new UserEntity();
        adminEntity.setId(99L);
        adminEntity.setUsername("adminUser");
        when(userRepository.findByUsername("adminUser")).thenReturn(Optional.of(adminEntity));

        Page<BookingSearchResponse> emptyPage = new PageImpl<>(Collections.emptyList());
        when(bookingService.getBookingsByUserId(eq(20L), any(BookingSearchRequest.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        ResponseEntity<?> response = userController.getBookingsByUserId(
                20L,
                new BookingSearchRequest(),
                0,
                10,
                new String[]{"id", "desc"},
                adminUserDetails
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(emptyPage, response.getBody());
        verify(bookingService, times(1)).getBookingsByUserId(eq(20L), any(), any());
    }

    @Test
    void deleteUser_Success() {
        doNothing().when(userService).delete(10L);

        ResponseEntity<?> response = userController.deleteUser(10L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService, times(1)).delete(10L);
    }

    @Test
    void deleteUser_ReturnsBadRequest_OnException() {
        doThrow(new RuntimeException("Lỗi xóa")).when(userService).delete(10L);

        ResponseEntity<?> response = userController.deleteUser(10L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());
    }

    @Test
    void updateUser_Success() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(10L);
        when(userService.update(userDTO)).thenReturn(userDTO);

        ResponseEntity<?> response = userController.updateUser(userDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userDTO, response.getBody());
        verify(userService, times(1)).update(userDTO);
    }
}
