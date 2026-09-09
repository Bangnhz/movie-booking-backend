package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.entity.CityEntity;
import com.moviebooking.movie_booking.model.dto.CityDTO;
import com.moviebooking.movie_booking.repository.CityRepository;
import com.moviebooking.movie_booking.service.CityService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CityServiceImpl implements CityService {
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Override
    public List<CityDTO> getAllCities() {
        List<CityEntity> cityEntities = cityRepository.findAll();
        List<CityDTO> cityDTOs = cityEntities.stream().map(cityEntity -> modelMapper.map(cityEntity,CityDTO.class)).toList();
        return cityDTOs;
    }
}
