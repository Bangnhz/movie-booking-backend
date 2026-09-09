package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.converter.GenreConverter;
import com.moviebooking.movie_booking.entity.GenreEntity;
import com.moviebooking.movie_booking.model.dto.GenreDTO;
import com.moviebooking.movie_booking.repository.GenreRepository;
import com.moviebooking.movie_booking.service.GenreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenreServiceImpl implements GenreService {
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private GenreConverter genreConverter;
    @Override
    public List<GenreDTO> getAll() {
        List<GenreDTO> genreDTOS = genreRepository.findAll().stream().map(genreConverter::toGenreDTO).toList();
        return genreDTOS;
    }

    @Override
    public void create(GenreDTO genreDTO) {
        if (genreRepository.findByName(genreDTO.getName()).isPresent()) {
            throw new RuntimeException("Đã tồn tại genre: " + genreDTO.getName());
        }
        GenreEntity genreEntity = genreConverter.toGenreEntity(genreDTO);
        if(genreRepository.findByName(genreDTO.getName()).orElse(null) != null) {
            throw new RuntimeException("Đã tổn tại genre:"+genreDTO.getName());
        }
        genreRepository.save(genreEntity);
    }

    @Override
    public void delete(Long id) {
        GenreEntity genreEntity = genreRepository.findById(id).orElse(null);
        genreRepository.delete(genreEntity);
    }

    @Override
    public void update(GenreDTO genreDTO) {
        GenreEntity oldGenre = genreRepository.findById(genreDTO.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy genre"));
        oldGenre.setName(genreDTO.getName());
        genreRepository.save(oldGenre);
    }
}

