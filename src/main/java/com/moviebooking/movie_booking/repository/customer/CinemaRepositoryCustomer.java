package com.moviebooking.movie_booking.repository.customer;

import com.moviebooking.movie_booking.model.dto.CinemaListDTO;
import com.moviebooking.movie_booking.model.request.search.CinemaSearchRequest;
import com.moviebooking.movie_booking.model.response.CinemaSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CinemaRepositoryCustomer {
    public List<Object[]> findAll(CinemaSearchRequest request, Pageable pageable);
    public Long countTotal(CinemaSearchRequest request);
}
