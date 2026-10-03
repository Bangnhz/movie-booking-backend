package com.moviebooking.movie_booking.converter;

import com.moviebooking.movie_booking.entity.CinemaEntity;
import com.moviebooking.movie_booking.model.dto.CinemaDetailDTO;
import com.moviebooking.movie_booking.model.dto.CityDTO;
import com.moviebooking.movie_booking.model.request.save.CinemaSaveRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CinemaConverter {
    @Autowired
    private ModelMapper modelMapper;

    public CinemaEntity toCinemaEntity(CinemaSaveRequest request) {
        return modelMapper.map(request, CinemaEntity.class);
    }

    public CinemaDetailDTO toCinemaDetailDTO(CinemaEntity cinemaEntity) {
        if (cinemaEntity == null) return null;
        CinemaDetailDTO dto = new CinemaDetailDTO();
        dto.setId(cinemaEntity.getId());
        dto.setName(cinemaEntity.getName());
        dto.setAddress(cinemaEntity.getAddress());
        dto.setEmail(cinemaEntity.getEmail());
        if (cinemaEntity.getCity() != null) {
            CityDTO cityDTO = new CityDTO();
            cityDTO.setId(cinemaEntity.getCity().getId());
            cityDTO.setName(cinemaEntity.getCity().getName());
            dto.setCity(cityDTO);
        }
        return dto;
    }
}
