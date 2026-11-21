package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.config.ModelMapperConfig;
import com.moviebooking.movie_booking.entity.GenreEntity;
import com.moviebooking.movie_booking.entity.MovieEntity;
import com.moviebooking.movie_booking.enums.AgeRating;
import com.moviebooking.movie_booking.model.request.save.MovieSaveRequest;
import com.moviebooking.movie_booking.repository.GenreRepository;
import com.moviebooking.movie_booking.repository.MovieGenreRepository;
import com.moviebooking.movie_booking.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceImplTests {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieGenreRepository movieGenreRepository;

    @Mock
    private GenreRepository genreRepository;

    @Spy
    private ModelMapper modelMapper = new ModelMapperConfig().modelMapper();

    @InjectMocks
    private MovieServiceImpl movieService;

    @Test
    void update_PreservesEntityIdFromUrl_WhenMappingRequest() {
        Long targetId = 7L;

        MovieEntity existingMovie = new MovieEntity();
        existingMovie.setId(targetId);
        existingMovie.setTitle("Old Title");
        existingMovie.setDescription("Old Description");
        existingMovie.setDuration(100);

        when(movieRepository.findById(targetId)).thenReturn(Optional.of(existingMovie));
        when(genreRepository.findAllById(any())).thenReturn(List.of(new GenreEntity()));

        MovieSaveRequest request = new MovieSaveRequest();
        request.setTitle("New Updated Title");
        request.setDescription("New Description");
        request.setDuration(120);
        request.setAgeRating(AgeRating.T18);
        request.setReleaseDate(LocalDate.now());
        request.setGenreIds(List.of(1L, 2L));

        movieService.update(request, targetId);

        // Verify entity id is retained and not overwritten with null
        assertNotNull(existingMovie.getId());
        assertEquals(targetId, existingMovie.getId());
        assertEquals("New Updated Title", existingMovie.getTitle());
        assertEquals("New Description", existingMovie.getDescription());
        assertEquals(120, existingMovie.getDuration());

        verify(movieRepository, times(1)).save(existingMovie);
    }
}
