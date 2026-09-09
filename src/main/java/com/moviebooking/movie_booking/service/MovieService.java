package com.moviebooking.movie_booking.service;

import com.moviebooking.movie_booking.model.dto.MovieCardDTO;
import com.moviebooking.movie_booking.model.dto.MovieDetailDTO;
import com.moviebooking.movie_booking.model.request.save.MovieSaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface MovieService {
    List<MovieCardDTO> getNowShowing();
    List<MovieCardDTO> getComingSoon();
    MovieDetailDTO getMovieDetail(Long id);
    List<MovieCardDTO> getMovies();
    Page<MovieCardDTO> findAll(Pageable pageable);
    void update(MovieSaveRequest request,Long id);
    void add(MovieSaveRequest request);

}
