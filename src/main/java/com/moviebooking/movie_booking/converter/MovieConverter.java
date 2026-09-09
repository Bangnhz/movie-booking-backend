package com.moviebooking.movie_booking.converter;

import com.moviebooking.movie_booking.entity.GenreEntity;
import com.moviebooking.movie_booking.entity.MovieGenreEntity;
import com.moviebooking.movie_booking.enums.AgeRating;
import com.moviebooking.movie_booking.model.dto.GenreDTO;
import com.moviebooking.movie_booking.model.dto.MovieBookingDTO;
import com.moviebooking.movie_booking.model.dto.MovieCardDTO;
import com.moviebooking.movie_booking.entity.MovieEntity;
import com.moviebooking.movie_booking.model.dto.MovieDetailDTO;
import com.moviebooking.movie_booking.model.request.save.MovieSaveRequest;
import com.moviebooking.movie_booking.repository.GenreRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MovieConverter {
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private GenreRepository genreRepository;

    public MovieCardDTO toMovieCardDTO(MovieEntity movieEntity) {
        MovieCardDTO movieCardDTO = modelMapper.map(movieEntity, MovieCardDTO.class);
        return movieCardDTO;
    }
    public MovieDetailDTO toMovieDetailDTO(MovieEntity movieEntity) {
        MovieDetailDTO movieDetailDTO = modelMapper.map(movieEntity, MovieDetailDTO.class);
        List<GenreEntity> genresByMovie = movieEntity.getMovieGenres().stream()
                .map(movieGenre -> movieGenre.getGenre()).toList();
        List<GenreDTO> genreDTOsByMovie = genresByMovie.stream().map(genre -> modelMapper.map(genre, GenreDTO.class)).toList();
        movieDetailDTO.setGenres(genreDTOsByMovie);
        return movieDetailDTO;
    }
    public MovieBookingDTO toMovieBookingDTO(MovieEntity movieEntity) {
        MovieBookingDTO movieBookingDTO = modelMapper.map(movieEntity, MovieBookingDTO.class);

        AgeRating ageRating = movieEntity.getAgeRating();
        movieBookingDTO.setAgeRatingName(ageRating.name());
        movieBookingDTO.setAgeRatingColor(ageRating.getColor());
        return movieBookingDTO;
    }
    public MovieEntity toMovieEntity(MovieSaveRequest request) {
        MovieEntity movieEntity = modelMapper.map(request, MovieEntity.class);
        List<MovieGenreEntity> movieGenreEntities = new ArrayList<>();
        if(request.getGenreIds() != null &&  !request.getGenreIds().isEmpty()) {
            List<GenreEntity> genreEntities = genreRepository.findAllById(request.getGenreIds());
            movieGenreEntities= genreEntities.stream()
                    .map(genre -> {
                        MovieGenreEntity movieGenreEntity = new MovieGenreEntity();
                        movieGenreEntity.setGenre(genre);
                        movieGenreEntity.setMovie(movieEntity);
                        return movieGenreEntity;
                    }).toList();
        }
        movieEntity.setMovieGenres(movieGenreEntities);
        return movieEntity;
    }
}
