package com.moviebooking.movie_booking.repository;

import com.moviebooking.movie_booking.entity.CinemaEntity;
import com.moviebooking.movie_booking.repository.customer.CinemaRepositoryCustomer;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CinemaRepository extends JpaRepository<CinemaEntity,Long>, CinemaRepositoryCustomer {
    List<CinemaEntity> findByCityId(Long cityId);
    List<CinemaEntity> findByCityName(String cityName);

    boolean findByName(String name);
}
