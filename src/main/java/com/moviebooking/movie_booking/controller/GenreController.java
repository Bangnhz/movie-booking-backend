package com.moviebooking.movie_booking.controller;

import com.moviebooking.movie_booking.model.dto.GenreDTO;
import com.moviebooking.movie_booking.service.GenreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/genres")
public class GenreController {
    @Autowired
    private GenreService genreService;

    @GetMapping
    public ResponseEntity<List<GenreDTO>> getAll() {
        List<GenreDTO> genreDTOS = genreService.getAll();
        return ResponseEntity.ok(genreDTOS);
    }
    @PostMapping
    public ResponseEntity<?> create(@RequestBody GenreDTO genreDTO) {
        try {
            genreService.create(genreDTO);
            return ResponseEntity.ok(genreDTO);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }
    @PutMapping
    public ResponseEntity<?> update(@RequestBody GenreDTO genreDTO) {
        try {
            genreService.create(genreDTO);
            return ResponseEntity.ok(genreDTO);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            genreService.delete(id);
            return ResponseEntity.noContent().build();
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }
}
