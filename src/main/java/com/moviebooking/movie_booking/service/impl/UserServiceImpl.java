package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.config.security.JwtUtils;
import com.moviebooking.movie_booking.converter.UserConverter;
import com.moviebooking.movie_booking.entity.RoleEntity;
import com.moviebooking.movie_booking.entity.UserEntity;
import com.moviebooking.movie_booking.entity.UserRoleEntity;
import com.moviebooking.movie_booking.entity.embedded_id.UserRoleId;
import com.moviebooking.movie_booking.model.dto.RoleDTO;
import com.moviebooking.movie_booking.model.dto.UserDTO;
import com.moviebooking.movie_booking.model.request.LoginRequest;
import com.moviebooking.movie_booking.model.request.RegisterRequest;
import com.moviebooking.movie_booking.model.request.search.UserSearchRequest;
import com.moviebooking.movie_booking.model.response.LoginResponse;
import com.moviebooking.movie_booking.repository.RoleRepository;
import com.moviebooking.movie_booking.repository.UserRepository;
import com.moviebooking.movie_booking.repository.UserRoleRepository;
import com.moviebooking.movie_booking.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
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
    @Transactional
    public void delete(Long id) {
        UserEntity userEntity = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User không tồn tại"));
        userRoleRepository.deleteByUserId(id);
        userRoleRepository.flush();
        userRepository.delete(userEntity);
    }

    @Override
    @Transactional
    public UserDTO update(UserDTO userDTO) {
        if (userDTO.getId() == null) {
            throw new IllegalArgumentException("ID người dùng không được để trống");
        }
        UserEntity userEntity = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (userDTO.getFullname() != null && !userDTO.getFullname().trim().isEmpty()) {
            userEntity.setFullname(userDTO.getFullname().trim());
        }
        if (userDTO.getEmail() != null && !userDTO.getEmail().trim().isEmpty()) {
            String newEmail = userDTO.getEmail().trim();
            if (!newEmail.equalsIgnoreCase(userEntity.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new RuntimeException("Email đã tồn tại!");
            }
            userEntity.setEmail(newEmail);
        }
        if (userDTO.getPhone() != null && !userDTO.getPhone().trim().isEmpty()) {
            String newPhone = userDTO.getPhone().trim();
            if (!newPhone.equalsIgnoreCase(userEntity.getPhone()) && userRepository.existsByPhone(newPhone)) {
                throw new RuntimeException("Số điện thoại đã tồn tại!");
            }
            userEntity.setPhone(newPhone);
        }
        if (userDTO.getPoints() != null) {
            userEntity.setPoints(userDTO.getPoints());
        }

        String targetRoleName = null;
        if (userDTO.getRole() != null && !userDTO.getRole().trim().isEmpty()) {
            targetRoleName = userDTO.getRole().trim();
        } else if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
            RoleDTO first = userDTO.getRoles().get(0);
            if (first != null && first.getName() != null && !first.getName().trim().isEmpty()) {
                targetRoleName = first.getName().trim();
            }
        }

        if (targetRoleName != null) {
            String cleanRoleName = targetRoleName.toUpperCase();
            if (cleanRoleName.startsWith("ROLE_")) {
                cleanRoleName = cleanRoleName.substring(5);
            }
            final String finalRoleName = cleanRoleName;
            RoleEntity roleEntity = roleRepository.findByNameIgnoreCase(finalRoleName)
                    .orElseGet(() -> {
                        RoleEntity newRole = new RoleEntity();
                        newRole.setName(finalRoleName);
                        return roleRepository.save(newRole);
                    });

            userRoleRepository.deleteByUserId(userEntity.getId());
            userRoleRepository.flush();

            UserRoleEntity userRole = new UserRoleEntity();
            userRole.setId(new UserRoleId(userEntity.getId(), roleEntity.getId()));
            userRole.setUser(userEntity);
            userRole.setRole(roleEntity);
            userRoleRepository.save(userRole);

            userEntity.setUserRoles(new ArrayList<>(List.of(userRole)));
        }

        userRepository.save(userEntity);

        UserDTO result = new UserDTO();
        result.setId(userEntity.getId());
        result.setFullname(userEntity.getFullname());
        result.setUsername(userEntity.getUsername());
        result.setEmail(userEntity.getEmail());
        result.setPhone(userEntity.getPhone());
        result.setPoints(userEntity.getPoints());

        List<RoleDTO> roleDTOs = new ArrayList<>();
        if (userEntity.getUserRoles() != null) {
            for (UserRoleEntity ur : userEntity.getUserRoles()) {
                if (ur.getRole() != null) {
                    roleDTOs.add(new RoleDTO(ur.getRole().getId(), ur.getRole().getName()));
                }
            }
        }
        result.setRoles(roleDTOs);
        if (!roleDTOs.isEmpty()) {
            result.setRole(roleDTOs.get(0).getName());
        }

        return result;
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
