package com.moviebooking.movie_booking.converter;

import com.moviebooking.movie_booking.entity.GenreEntity;
import com.moviebooking.movie_booking.model.dto.GenreDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GenreConverter {
    @Autowired
    private ModelMapper modelMapper;

    public GenreDTO toGenreDTO(GenreEntity genreEntity) {
        return modelMapper.map(genreEntity, GenreDTO.class);
    }
    public GenreEntity toGenreEntity(GenreDTO genreDTO) {
        return modelMapper.map(genreDTO, GenreEntity.class);
    }
}
