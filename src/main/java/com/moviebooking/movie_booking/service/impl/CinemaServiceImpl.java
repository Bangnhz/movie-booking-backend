package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.converter.CinemaConverter;
import com.moviebooking.movie_booking.converter.MovieConverter;
import com.moviebooking.movie_booking.entity.CinemaEntity;
import com.moviebooking.movie_booking.entity.MovieEntity;
import com.moviebooking.movie_booking.entity.ShowtimeEntity;
import com.moviebooking.movie_booking.model.dto.*;
import com.moviebooking.movie_booking.model.request.save.CinemaSaveRequest;
import com.moviebooking.movie_booking.model.request.search.CinemaSearchRequest;
import com.moviebooking.movie_booking.model.response.CinemaSearchResponse;
import com.moviebooking.movie_booking.repository.CinemaRepository;
import com.moviebooking.movie_booking.repository.ShowtimeRepository;
import com.moviebooking.movie_booking.service.CinemaService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Arrays.stream;

@Service
public class CinemaServiceImpl implements CinemaService {
    @Autowired
    private CinemaRepository cinemaRepository;
    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private MovieConverter movieConverter;
    @Autowired
    private CinemaConverter cinemaConverter;

    public CinemaScheduleDTO getCinemaSchedule(Long cinemaId, LocalDate date) {
        LocalDateTime start = LocalDateTime.now();
        CinemaEntity cinemaEntity = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy rạp này!"));
        List<MovieEntity> movieEntities = showtimeRepository.findMoviesWithShowtimes(cinemaId,start);
        List<MovieScheduleDTO> movieScheduleDTOs = movieEntities.stream()
                .map(m -> {
                    MovieScheduleDTO movieScheduleDTO = modelMapper.map(m, MovieScheduleDTO.class);
                    List<ShowtimeSlotDTO> slots = m.getShowtimes().stream().map(s -> modelMapper.map(s, ShowtimeSlotDTO.class))
                            .toList();
                    movieScheduleDTO.setShowtimes(slots);
                    return movieScheduleDTO;
                }).toList();

        CinemaScheduleDTO cinemaScheduleDTO = modelMapper.map(cinemaEntity, CinemaScheduleDTO.class);
        cinemaScheduleDTO.setMovies(movieScheduleDTOs);
        return cinemaScheduleDTO;
    }

    @Override
    public List<CinemaListDTO> getByCityId(Long cityId) {
        List<CinemaEntity> cinemaEntities = cinemaRepository.findByCityId(cityId);
        List<CinemaListDTO> cinemaListDTOs = cinemaEntities.stream().map(cinemaEntity -> {
            return modelMapper.map(cinemaEntity, CinemaListDTO.class);
        }).toList();
        return cinemaListDTOs;
    }

    @Override
    public CinemaDetailDTO getCinemaById(Long cinemaId) {
        CinemaEntity cinemaEntity = cinemaRepository.findById(cinemaId).orElseThrow(() -> new RuntimeException("Không tìm thấy rạp có id="+cinemaId));;
        return cinemaConverter.toCinemaDetailDTO(cinemaEntity);
    }

    @Override
    public Page<CinemaSearchResponse> findAll(CinemaSearchRequest request, Pageable pageable) {
        List<Object[]> results = cinemaRepository.findAll(request, pageable);
        List<CinemaSearchResponse> responses = results.stream().map(row -> {
            CinemaSearchResponse res = new CinemaSearchResponse();
            res.setId(((Number) row[0]).longValue());
            res.setName((String) row[1]);
            res.setAddress((String) row[2]);
            res.setEmail((String) row[3]);
            res.setCity((String) row[4]);
            res.setNumberOfRoom(row[5].toString());
            return res;
        }).toList();
        Long total = cinemaRepository.countTotal(request);
        return new PageImpl<>(responses, pageable, total);
    }

    @Override
    @Transactional
    public void add(CinemaSaveRequest request) {

        if (cinemaRepository.findByName(request.getName())) {
            throw new RuntimeException("Tên rạp phim đã tồn tại!");
        }

        try {
            CinemaEntity cinemaEntity = cinemaConverter.toCinemaEntity(request);
            cinemaRepository.save(cinemaEntity);
        } catch (DataAccessException e) {
            throw new RuntimeException("Lỗi khi lưu rạp phim vào database", e);
        }
    }

    @Override
    @Transactional
    public void update(CinemaSaveRequest request) {
        if (cinemaRepository.findByName(request.getName())) {
            throw new RuntimeException("Tên rạp phim đã tồn tại!");
        }

        try {
            CinemaEntity cinemaEntity = cinemaConverter.toCinemaEntity(request);
            cinemaRepository.save(cinemaEntity);
        } catch (DataAccessException e) {
            throw new RuntimeException("Lỗi khi cập nhật rạp phim vào database", e);
        }
    }

}
