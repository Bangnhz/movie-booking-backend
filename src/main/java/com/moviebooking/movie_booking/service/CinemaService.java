package com.moviebooking.movie_booking.service;

import com.moviebooking.movie_booking.entity.CinemaEntity;
import com.moviebooking.movie_booking.model.dto.CinemaDetailDTO;
import com.moviebooking.movie_booking.model.dto.CinemaListDTO;
import com.moviebooking.movie_booking.model.dto.CinemaScheduleDTO;
import com.moviebooking.movie_booking.model.request.save.CinemaSaveRequest;
import com.moviebooking.movie_booking.model.request.search.CinemaSearchRequest;
import com.moviebooking.movie_booking.model.response.CinemaSearchResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
@Service
public interface CinemaService {
    public CinemaScheduleDTO getCinemaSchedule(Long cinemaId, LocalDate date);
    List<CinemaListDTO> getByCityId(Long cityId);
    CinemaDetailDTO getCinemaById(Long cinemaId);
    Page<CinemaSearchResponse> findAll(CinemaSearchRequest request, Pageable pageable);
    void add(CinemaSaveRequest request);
    void update(CinemaSaveRequest request);
}