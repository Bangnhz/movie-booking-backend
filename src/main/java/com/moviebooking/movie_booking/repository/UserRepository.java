package com.moviebooking.movie_booking.repository;

import com.moviebooking.movie_booking.entity.UserEntity;
import com.moviebooking.movie_booking.repository.customer.UserRepositoryCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity,Long>, UserRepositoryCustomer {
    Optional<UserEntity> findByUsername(String username);
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByPhone(String phone);
}
