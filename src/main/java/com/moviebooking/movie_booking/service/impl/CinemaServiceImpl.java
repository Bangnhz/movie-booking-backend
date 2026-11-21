package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.converter.CinemaConverter;
import com.moviebooking.movie_booking.converter.MovieConverter;
import com.moviebooking.movie_booking.entity.CinemaEntity;
import com.moviebooking.movie_booking.entity.CityEntity;
import com.moviebooking.movie_booking.entity.MovieEntity;
import com.moviebooking.movie_booking.entity.ShowtimeEntity;
import com.moviebooking.movie_booking.model.dto.*;
import com.moviebooking.movie_booking.model.request.save.CinemaSaveRequest;
import com.moviebooking.movie_booking.model.request.search.CinemaSearchRequest;
import com.moviebooking.movie_booking.model.response.CinemaSearchResponse;
import com.moviebooking.movie_booking.repository.CinemaRepository;
import com.moviebooking.movie_booking.repository.CityRepository;
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

@Service
public class CinemaServiceImpl implements CinemaService {
    @Autowired
    private CinemaRepository cinemaRepository;
    @Autowired
    private ShowtimeRepository showtimeRepository;
    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private MovieConverter movieConverter;
    @Autowired
    private CinemaConverter cinemaConverter;

    public CinemaScheduleDTO getCinemaSchedule(Long cinemaId, LocalDate date) {
        LocalDateTime start = LocalDateTime.now();
        CinemaEntity cinemaEntity = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new RuntimeException("Cinema not found"));

        List<MovieEntity> movies = showtimeRepository.findMoviesWithShowtimes(cinemaId, start);
        List<MovieScheduleDTO> movieDTOs = movies.stream()
                .map(movieConverter::toMovieScheduleDTO)
                .toList();
        CinemaScheduleDTO cinemaScheduleDTO = new CinemaScheduleDTO();
        cinemaScheduleDTO.setId(cinemaEntity.getId());
        cinemaScheduleDTO.setName(cinemaEntity.getName());
        cinemaScheduleDTO.setMovies(movieDTOs);
        return cinemaScheduleDTO;
    }

    @Override
    public List<CinemaListDTO> getByCityId(Long cityId) {
        List<CinemaEntity> cinemaEntities = cinemaRepository.findByCityId(cityId);
        return cinemaEntities.stream()
                .map(cinemaEntity -> modelMapper.map(cinemaEntity, CinemaListDTO.class))
                .toList();
    }

    @Override
    public CinemaDetailDTO getCinemaById(Long id) {
        CinemaEntity cinemaEntity = cinemaRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Không tìm thấy cinema với id=" + id)
        );
        return cinemaConverter.toCinemaDetailDTO(cinemaEntity);
    }

    @Override
    public Page<CinemaSearchResponse> findAll(CinemaSearchRequest request, Pageable pageable) {
        List<Object[]> rows = cinemaRepository.findAll(request, pageable);
        List<CinemaSearchResponse> responses = rows.stream().map(row -> {
            CinemaSearchResponse res = new CinemaSearchResponse();
            if (row[0] != null) res.setId(((Number) row[0]).longValue());
            if (row[1] != null) res.setName((String) row[1]);
            if (row[2] != null) res.setAddress((String) row[2]);
            if (row[3] != null) res.setEmail((String) row[3]);
            if (row[4] != null) res.setCity((String) row[4]);
            res.setNumberOfRoom(row[5] != null ? String.valueOf(row[5]) : "0");
            return res;
        }).toList();
        Long total = cinemaRepository.countTotal(request);
        return new PageImpl<>(responses, pageable, total);
    }

    @Override
    @Transactional
    public void add(CinemaSaveRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new RuntimeException("Tên rạp không được để trống!");
        }
        String cinemaName = request.getName().trim();
        if (cinemaRepository.existsByName(cinemaName)) {
            throw new RuntimeException("Tên rạp phim đã tồn tại!");
        }

        try {
            CinemaEntity cinemaEntity = cinemaConverter.toCinemaEntity(request);
            cinemaEntity.setName(cinemaName);
            Long cityId = null;
            if (request.getCity() != null && request.getCity().getId() != null) {
                cityId = request.getCity().getId();
            } else if (request.getCityId() != null) {
                cityId = request.getCityId();
            }
            if (cityId != null) {
                Long finalCityId = cityId;
                CityEntity city = cityRepository.findById(finalCityId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy thành phố với id=" + finalCityId));
                cinemaEntity.setCity(city);
            }
            cinemaRepository.save(cinemaEntity);
        } catch (DataAccessException e) {
            throw new RuntimeException("Lỗi khi lưu rạp phim vào database", e);
        }
    }

    @Override
    @Transactional
    public void update(CinemaSaveRequest request, Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID rạp phim không được để trống!");
        }
        CinemaEntity existingCinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy rạp phim với id=" + id));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            String cinemaName = request.getName().trim();
            if (cinemaRepository.existsByNameAndIdNot(cinemaName, id)) {
                throw new RuntimeException("Tên rạp phim đã tồn tại!");
            }
            existingCinema.setName(cinemaName);
        }
        if (request.getAddress() != null) {
            existingCinema.setAddress(request.getAddress().trim());
        }
        if (request.getEmail() != null) {
            existingCinema.setEmail(request.getEmail().trim());
        }

        Long cityId = null;
        if (request.getCity() != null && request.getCity().getId() != null) {
            cityId = request.getCity().getId();
        } else if (request.getCityId() != null) {
            cityId = request.getCityId();
        }
        if (cityId != null) {
            Long finalCityId = cityId;
            CityEntity city = cityRepository.findById(finalCityId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thành phố với id=" + finalCityId));
            existingCinema.setCity(city);
        }

        try {
            cinemaRepository.save(existingCinema);
        } catch (DataAccessException e) {
            throw new RuntimeException("Lỗi khi cập nhật rạp phim vào database", e);
        }
    }
}
