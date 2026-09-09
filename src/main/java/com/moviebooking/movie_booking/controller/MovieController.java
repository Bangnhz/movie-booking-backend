package com.moviebooking.movie_booking.controller;

import com.moviebooking.movie_booking.model.dto.MovieCardDTO;
import com.moviebooking.movie_booking.model.request.save.MovieSaveRequest;
import com.moviebooking.movie_booking.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
@RestController
@RequestMapping("/api/movies")
@CrossOrigin(origins = "*")
public class MovieController {
    @Autowired
    private MovieService movieService;
    @GetMapping("/now-showing")
    public ResponseEntity<?> getNowShowing(){
        List<MovieCardDTO> movies = movieService.getNowShowing();
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/coming-soon")
    public ResponseEntity<List<MovieCardDTO>> getComingSoon() {
        List<MovieCardDTO> movies = movieService.getComingSoon();
        return ResponseEntity.ok(movies);
    }
    @GetMapping("/{movieId}")
    public ResponseEntity<?> getMovie(@PathVariable Long movieId){
        return ResponseEntity.ok(movieService.getMovieDetail(movieId));
    }
    @GetMapping
    public ResponseEntity<?> getMovies(){
        List<MovieCardDTO> movieCardDTOS =  movieService.getMovies();
        return ResponseEntity.ok(movieCardDTOS);
    }
    @GetMapping("/list")
    public ResponseEntity<Page<MovieCardDTO>> getMovies(
            @RequestParam(defaultValue = "0") int page,    // Trang hiện tại (bắt đầu từ 0)
            @RequestParam(defaultValue = "10") int size,   // Số lượng bản ghi mỗi trang
            @RequestParam(defaultValue = "id,desc") String[] sort // Sắp xếp (ví dụ: id giảm dần)
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort[0]).descending());

        if (sort[1].equalsIgnoreCase("asc")) {
            pageable = PageRequest.of(page, size, Sort.by(sort[0]).ascending());
        }

        Page<MovieCardDTO> result = movieService.findAll(pageable);
        return ResponseEntity.ok(result);
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMovie(@PathVariable Long id, @RequestBody MovieSaveRequest movieSaveRequest){
        try{
            movieService.update(movieSaveRequest,id);
            return ResponseEntity.ok(Map.of("message", "Cập nhật thành công"));        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }
    @PostMapping
    public ResponseEntity<?> addMovie(@RequestBody MovieSaveRequest movieSaveRequest){
        try{
            movieService.add(movieSaveRequest);
            return ResponseEntity.ok(Map.of("message", "Thêm thành công"));        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }
}
