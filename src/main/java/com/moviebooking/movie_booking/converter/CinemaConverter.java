package com.moviebooking.movie_booking.converter;

import com.moviebooking.movie_booking.entity.CinemaEntity;
import com.moviebooking.movie_booking.model.request.save.CinemaSaveRequest;
import com.moviebooking.movie_booking.model.request.search.CinemaSearchRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CinemaConverter {
    @Autowired
    private ModelMapper modelMapper;
    public CinemaEntity toCinemaEntity(CinemaSaveRequest request) {
        CinemaEntity cinemaEntity = modelMapper.map(request, CinemaEntity.class);
        return cinemaEntity;
    }
}
