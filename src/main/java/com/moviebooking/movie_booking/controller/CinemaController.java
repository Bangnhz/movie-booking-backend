package com.moviebooking.movie_booking.controller;

import com.moviebooking.movie_booking.model.dto.CinemaDetailDTO;
import com.moviebooking.movie_booking.model.dto.CinemaListDTO;
import com.moviebooking.movie_booking.model.dto.CinemaScheduleDTO;
import com.moviebooking.movie_booking.model.request.save.CinemaSaveRequest;
import com.moviebooking.movie_booking.model.request.search.CinemaSearchRequest;
import com.moviebooking.movie_booking.model.response.CinemaSearchResponse;
import com.moviebooking.movie_booking.service.CinemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cinemas")
@CrossOrigin(origins = "*")
public class CinemaController {
    @Autowired

    private CinemaService cinemaService;

    @GetMapping("/city/{cityId}")
    public ResponseEntity<List<CinemaListDTO>> getCinemasByCity(@PathVariable Long cityId) {
        List<CinemaListDTO> cinemaListDTOs = cinemaService.getByCityId(cityId);
        return ResponseEntity.ok(cinemaListDTOs);
    }
    @GetMapping("/{cinemaId}")
    public ResponseEntity<CinemaDetailDTO> getCinemaById(@PathVariable Long cinemaId) {
        CinemaDetailDTO cinemaDetailDTO = cinemaService.getCinemaById(cinemaId);
        return ResponseEntity.ok(cinemaDetailDTO);
    }
    @GetMapping("/{cinemaId}/schedule")
    public ResponseEntity<CinemaScheduleDTO> getCinemaSchedule(
            @PathVariable Long cinemaId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){
        if (date == null) {
            date = LocalDate.now();
        }
        CinemaScheduleDTO schedule = cinemaService.getCinemaSchedule(cinemaId, date);
        return ResponseEntity.ok(schedule);
    }
    @GetMapping
    public ResponseEntity<Page<CinemaSearchResponse>> getCinemaSearch(
            @ModelAttribute CinemaSearchRequest cinemaSearchRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort){
        Sort.Direction direction = Sort.Direction.fromString(sort[1]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction,sort[0]));
        Page<CinemaSearchResponse> cinemaSearchResponses = cinemaService.findAll(cinemaSearchRequest, pageable);
        return ResponseEntity.ok(cinemaSearchResponses);
    }
    @PostMapping
    public ResponseEntity<?> createCinema(
            @RequestBody CinemaSaveRequest request
            ){
        try {
            cinemaService.add(request);
            return ResponseEntity.ok(Map.of("message", "Thêm thành công"));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }
    @PutMapping("{id}")
    public ResponseEntity<?> updateCinema(
            @PathVariable Long id,
            @RequestBody CinemaSaveRequest request
    ){
        try {
            cinemaService.update(request);
            return ResponseEntity.ok(Map.of("message", "Cập nhật thành công"));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }
}
