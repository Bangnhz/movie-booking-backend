package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.converter.MovieConverter;
import com.moviebooking.movie_booking.entity.GenreEntity;
import com.moviebooking.movie_booking.entity.MovieEntity;
import com.moviebooking.movie_booking.entity.MovieGenreEntity;
import com.moviebooking.movie_booking.model.dto.MovieCardDTO;
import com.moviebooking.movie_booking.model.dto.MovieDetailDTO;
import com.moviebooking.movie_booking.model.request.save.MovieSaveRequest;
import com.moviebooking.movie_booking.repository.GenreRepository;
import com.moviebooking.movie_booking.repository.MovieGenreRepository;
import com.moviebooking.movie_booking.repository.MovieRepository;
import com.moviebooking.movie_booking.service.MovieService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MovieServiceImpl implements MovieService {
    @Autowired
    private MovieConverter movieConverter;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private MovieGenreRepository movieGenreRepository;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<MovieCardDTO> getNowShowing(){
        List<MovieEntity> movieEntities = movieRepository.findAllNowShowing();
        List<MovieCardDTO> movieCardDTOS = movieEntities.stream().map(movieConverter::toMovieCardDTO).toList();
        return movieCardDTOS;
    }

    @Override
    public List<MovieCardDTO> getComingSoon(){
        List<MovieEntity> movieEntities = movieRepository.findAllComingSoon();
        List<MovieCardDTO> movieCardDTOS = movieEntities.stream().map(movieConverter::toMovieCardDTO).toList();
        return movieCardDTOS;
    }

    @Override
    public MovieDetailDTO getMovieDetail(Long id) {
        MovieEntity movieEntity = movieRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Không tìm thấy movie với id="+id)
        );
        return movieConverter.toMovieDetailDTO(movieEntity);
    }

    @Override
    public List<MovieCardDTO> getMovies() {
        List<MovieEntity> movieEntities= movieRepository.findAll();
        List<MovieCardDTO> movieCardDTOS = movieEntities.stream().map(movieConverter::toMovieCardDTO).toList();
        return movieCardDTOS;
    }

    @Override
    public Page<MovieCardDTO> findAll(Pageable pageable) {
        Page<MovieEntity> entities = movieRepository.findAll(pageable);
        Page<MovieCardDTO> movieCardDTOS = entities.map(movieConverter::toMovieCardDTO);
        return movieCardDTOS;
    }

    @Override
    @Transactional
    public void update(MovieSaveRequest request, Long id) {
        // 1. Tìm Movie cũ (Chỉ để lấy thông tin cơ bản)
        MovieEntity movieEntity = movieRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Không tìm thấy movie với id=" + id)
        );

        // 2. Cập nhật thông tin cơ bản của Movie
        modelMapper.map(request, movieEntity);
        movieRepository.save(movieEntity);

        // 3. XỬ LÝ THỦ CÔNG BẢNG TRUNG GIAN
        if (request.getGenreIds() != null) {

            // BƯỚC A: Xóa thủ công các bản ghi cũ trong DB bằng Repository của bảng trung gian
            // Bạn phải tự viết hàm deleteByMovieId trong MovieGenreRepository
            movieGenreRepository.deleteByMovieId(id);
            movieGenreRepository.flush();
            // BƯỚC B: Lấy các GenreEntity mới
            List<GenreEntity> genreEntities = genreRepository.findAllById(request.getGenreIds());

            // BƯỚC C: Tạo danh sách các thực thể trung gian mới
            List<MovieGenreEntity> movieGenreEntities = genreEntities.stream()
                    .map(genre -> {
                        MovieGenreEntity mg = new MovieGenreEntity();
                        mg.setMovie(movieEntity);
                        mg.setGenre(genre);
                        return mg;
                    }).toList();
            movieGenreRepository.saveAll(movieGenreEntities);
        }
    }

    @Override
    public void add(MovieSaveRequest request) {
        MovieEntity movieEntity = movieConverter.toMovieEntity(request);
        movieRepository.save(movieEntity);
        if (request.getGenreIds() != null) {
            List<GenreEntity> genreEntities = genreRepository.findAllById(request.getGenreIds());
            List<MovieGenreEntity> movieGenreEntities = genreEntities.stream()
                    .map(genre -> {
                        MovieGenreEntity mg = new MovieGenreEntity();
                        mg.setMovie(movieEntity);
                        mg.setGenre(genre);
                        return mg;
                    }).toList();
            movieGenreRepository.saveAll(movieGenreEntities);
        }
    }

}
