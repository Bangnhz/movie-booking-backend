package com.moviebooking.movie_booking.repository.customer;

import com.moviebooking.movie_booking.model.request.search.BookingSearchRequest;
import org.springframework.data.domain.Pageable;
import java.util.*;

public interface BookingRepositoryCustomer {
    public List<Object[]> findByUserId(Long userId, BookingSearchRequest bookingSearchRequest, Pageable pageable);
    public Long countTotal(Long userId, BookingSearchRequest bookingSearchRequest);
}
