package com.moviebooking.movie_booking.repository.customer;

import com.moviebooking.movie_booking.model.dto.UserDTO;
import com.moviebooking.movie_booking.model.request.search.CinemaSearchRequest;
import com.moviebooking.movie_booking.model.request.search.UserSearchRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserRepositoryCustomer {
    public List<UserDTO> findAll(UserSearchRequest request, Pageable pageable);
    public Long countTotal(UserSearchRequest request);
}
