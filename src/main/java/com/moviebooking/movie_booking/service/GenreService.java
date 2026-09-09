package com.moviebooking.movie_booking.service;

import com.moviebooking.movie_booking.model.dto.GenreDTO;

import java.util.List;

public interface GenreService {
    List<GenreDTO> getAll();
    void create(GenreDTO genreDTO);
    void delete(Long id);
    void update(GenreDTO genreDTO);
}
