package com.moviebooking.movie_booking.converter;

import com.moviebooking.movie_booking.entity.UserEntity;
import com.moviebooking.movie_booking.model.request.RegisterRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserConverter {
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserEntity toUserEntity(RegisterRequest request) {
        UserEntity user = new UserEntity();
        user.setFullname(request.getFullname());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPoints(0);
        return user;
    }
}
