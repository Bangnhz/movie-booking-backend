package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.config.security.JwtUtils;
import com.moviebooking.movie_booking.converter.UserConverter;
import com.moviebooking.movie_booking.entity.RoleEntity;
import com.moviebooking.movie_booking.entity.UserEntity;
import com.moviebooking.movie_booking.entity.UserRoleEntity;
import com.moviebooking.movie_booking.model.dto.RoleDTO;
import com.moviebooking.movie_booking.model.dto.UserDTO;
import com.moviebooking.movie_booking.model.request.LoginRequest;
import com.moviebooking.movie_booking.model.request.RegisterRequest;
import com.moviebooking.movie_booking.model.request.search.UserSearchRequest;
import com.moviebooking.movie_booking.model.response.LoginResponse;
import com.moviebooking.movie_booking.repository.UserRepository;
import com.moviebooking.movie_booking.service.UserService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserConverter userConverter;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public void register(RegisterRequest request) {
        if(userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Số điện thoại đã tồn tại!");
        }
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại!");
        }
        UserEntity user = userConverter.toUserEntity(request);
        userRepository.save(user);
    }

    @Override
    public void delete(Long id) {
        UserEntity userEntity = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User không tồn tại"));
        userRepository.delete(userEntity);
    }

    @Override
    public void update(UserDTO userDTO) {
        UserEntity userEntity = userRepository.findById(userDTO.getId()).orElseThrow(() -> new RuntimeException("User không tồn tại"));
        userRepository.save(userEntity);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        String username = authentication.getName();
        String token = jwtUtils.generateToken(username);
        LoginResponse response= new LoginResponse(token, username);
        UserEntity user = userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("User không tồn tại"));
        List<RoleEntity> userRoles =  user.getUserRoles().stream()
                .map(userRole -> userRole.getRole()).toList();
        List<RoleDTO> roleDTOs = userRoles.stream()
                        .map(roleEntity -> modelMapper.map(roleEntity,RoleDTO.class))
                        .toList();
        response.setRoles(roleDTOs);
        return response;
    }

    @Override
    public Page<UserDTO> findAll(UserSearchRequest userSearchRequest, Pageable pageable) {
        List<UserDTO> userDTOS = userRepository.findAll(userSearchRequest, pageable);
        Long total = userRepository.countTotal(userSearchRequest);
        return new PageImpl<>(userDTOS, pageable, total);
    }
}
