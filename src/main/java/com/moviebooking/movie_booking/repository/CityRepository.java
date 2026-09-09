package com.moviebooking.movie_booking.repository;

import com.moviebooking.movie_booking.entity.CityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CityRepository extends JpaRepository<CityEntity,Long> {
    @Query("SELECT DISTINCT c FROM CityEntity c LEFT JOIN FETCH c.cinemas")
    List<CityEntity> findAllWithCinemas();
}
