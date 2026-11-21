package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.converter.CinemaConverter;
import com.moviebooking.movie_booking.entity.CinemaEntity;
import com.moviebooking.movie_booking.entity.CityEntity;
import com.moviebooking.movie_booking.model.dto.CityDTO;
import com.moviebooking.movie_booking.model.request.save.CinemaSaveRequest;
import com.moviebooking.movie_booking.repository.CinemaRepository;
import com.moviebooking.movie_booking.repository.CityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CinemaServiceImplTests {

    @Mock
    private CinemaRepository cinemaRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private CinemaConverter cinemaConverter;

    @InjectMocks
    private CinemaServiceImpl cinemaService;

    private CinemaEntity existingCinema;
    private CityEntity cityEntity;

    @BeforeEach
    void setUp() {
        cityEntity = new CityEntity();
        cityEntity.setId(1L);
        cityEntity.setName("Hà Nội");

        existingCinema = new CinemaEntity();
        existingCinema.setId(5L);
        existingCinema.setName("CGV Vincom");
        existingCinema.setAddress("Hai Ba Trung");
        existingCinema.setEmail("cgv@example.com");
        existingCinema.setCity(cityEntity);
    }

    @Test
    void add_ThrowsException_WhenNameAlreadyExists() {
        CinemaSaveRequest request = new CinemaSaveRequest();
        request.setName("CGV Vincom");
        when(cinemaRepository.existsByName("CGV Vincom")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> cinemaService.add(request));
        assertTrue(ex.getMessage().contains("đã tồn tại"));
    }

    @Test
    void add_Success_SavesCinemaWithCity() {
        CinemaSaveRequest request = new CinemaSaveRequest();
        request.setName("CGV Landmark");
        request.setCityId(1L);

        when(cinemaRepository.existsByName("CGV Landmark")).thenReturn(false);
        CinemaEntity newEntity = new CinemaEntity();
        when(cinemaConverter.toCinemaEntity(request)).thenReturn(newEntity);
        when(cityRepository.findById(1L)).thenReturn(Optional.of(cityEntity));

        cinemaService.add(request);

        assertEquals("CGV Landmark", newEntity.getName());
        assertEquals(cityEntity, newEntity.getCity());
        verify(cinemaRepository, times(1)).save(newEntity);
    }

    @Test
    void update_ThrowsException_WhenCinemaNotFound() {
        CinemaSaveRequest request = new CinemaSaveRequest();
        when(cinemaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> cinemaService.update(request, 99L));
    }

    @Test
    void update_ThrowsException_WhenNameExistsForAnotherCinema() {
        CinemaSaveRequest request = new CinemaSaveRequest();
        request.setName("Lotte Cinema");

        when(cinemaRepository.findById(5L)).thenReturn(Optional.of(existingCinema));
        when(cinemaRepository.existsByNameAndIdNot("Lotte Cinema", 5L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> cinemaService.update(request, 5L));
        assertTrue(ex.getMessage().contains("đã tồn tại"));
    }

    @Test
    void update_Success_WhenSameNameKeptOrCityUpdated() {
        CinemaSaveRequest request = new CinemaSaveRequest();
        request.setName("CGV Vincom");
        request.setAddress("Updated Address");
        request.setEmail("updated@example.com");
        CityDTO cityDto = new CityDTO();
        cityDto.setId(1L);
        cityDto.setName("Hà Nội");
        request.setCity(cityDto);

        when(cinemaRepository.findById(5L)).thenReturn(Optional.of(existingCinema));
        when(cinemaRepository.existsByNameAndIdNot("CGV Vincom", 5L)).thenReturn(false);
        when(cityRepository.findById(1L)).thenReturn(Optional.of(cityEntity));

        cinemaService.update(request, 5L);

        assertEquals("CGV Vincom", existingCinema.getName());
        assertEquals("Updated Address", existingCinema.getAddress());
        assertEquals("updated@example.com", existingCinema.getEmail());
        verify(cinemaRepository, times(1)).save(existingCinema);
    }
}
