package com.moviebooking.movie_booking.repository;

import com.moviebooking.movie_booking.entity.MovieGenreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieGenreRepository extends JpaRepository<MovieGenreEntity, Long> {
    void deleteByMovieId(Long movieId);
}
