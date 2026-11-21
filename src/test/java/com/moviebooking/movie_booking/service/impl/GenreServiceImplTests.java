package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.entity.GenreEntity;
import com.moviebooking.movie_booking.model.dto.GenreDTO;
import com.moviebooking.movie_booking.repository.GenreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenreServiceImplTests {

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private GenreServiceImpl genreService;

    private GenreEntity existingGenre;

    @BeforeEach
    void setUp() {
        existingGenre = new GenreEntity();
        existingGenre.setId(1L);
        existingGenre.setName("Hành động");
    }

    @Test
    void update_ThrowsException_WhenIdIsNull() {
        GenreDTO dto = new GenreDTO();
        assertThrows(IllegalArgumentException.class, () -> genreService.update(dto));
    }

    @Test
    void update_ThrowsException_WhenGenreNotFound() {
        GenreDTO dto = new GenreDTO(99L, "Kinh dị");
        when(genreRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> genreService.update(dto));
    }

    @Test
    void update_ThrowsException_WhenDuplicateNameOnOtherGenre() {
        GenreDTO dto = new GenreDTO(1L, "Kinh dị");
        when(genreRepository.findById(1L)).thenReturn(Optional.of(existingGenre));
        when(genreRepository.existsByNameAndIdNot("Kinh dị", 1L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> genreService.update(dto));
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("Kinh dị") || ex.getMessage().contains("tồn tại"));
    }

    @Test
    void update_Success_WhenSameNameOrUniqueName() {
        GenreDTO dto = new GenreDTO(1L, "Hành động");
        when(genreRepository.findById(1L)).thenReturn(Optional.of(existingGenre));
        when(genreRepository.existsByNameAndIdNot("Hành động", 1L)).thenReturn(false);

        genreService.update(dto);

        assertEquals("Hành động", existingGenre.getName());
        verify(genreRepository, times(1)).save(existingGenre);
    }
}
