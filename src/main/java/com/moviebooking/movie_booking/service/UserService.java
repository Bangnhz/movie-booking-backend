package com.moviebooking.movie_booking.service;

import com.moviebooking.movie_booking.model.dto.UserDTO;
import com.moviebooking.movie_booking.model.request.LoginRequest;
import com.moviebooking.movie_booking.model.request.RegisterRequest;
import com.moviebooking.movie_booking.model.request.search.UserSearchRequest;
import com.moviebooking.movie_booking.model.response.LoginResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    public void register(RegisterRequest register);
    public void delete(Long id);
    public void update(UserDTO userDTO);
    public LoginResponse login(LoginRequest login);
    Page<UserDTO> findAll(UserSearchRequest userSearchRequest, Pageable pageable);
}
