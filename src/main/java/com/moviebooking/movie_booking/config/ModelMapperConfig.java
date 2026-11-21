package com.moviebooking.movie_booking.config;

import com.moviebooking.movie_booking.entity.MovieEntity;
import com.moviebooking.movie_booking.model.request.save.MovieSaveRequest;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);

        modelMapper.typeMap(MovieSaveRequest.class, MovieEntity.class)
                .addMappings(mapper -> mapper.skip(MovieEntity::setId));

        return modelMapper;
    }
}
