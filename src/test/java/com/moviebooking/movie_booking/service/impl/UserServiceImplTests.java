package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.entity.RoleEntity;
import com.moviebooking.movie_booking.entity.UserEntity;
import com.moviebooking.movie_booking.entity.UserRoleEntity;
import com.moviebooking.movie_booking.model.dto.RoleDTO;
import com.moviebooking.movie_booking.model.dto.UserDTO;
import com.moviebooking.movie_booking.repository.RoleRepository;
import com.moviebooking.movie_booking.repository.UserRepository;
import com.moviebooking.movie_booking.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UserEntity existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new UserEntity();
        existingUser.setId(1L);
        existingUser.setUsername("testuser");
        existingUser.setFullname("Old Name");
        existingUser.setEmail("old@example.com");
        existingUser.setPhone("0901111111");
        existingUser.setPoints(50);
        existingUser.setUserRoles(new ArrayList<>());
    }

    @Test
    void update_ThrowsException_WhenIdIsNull() {
        UserDTO dto = new UserDTO();
        assertThrows(IllegalArgumentException.class, () -> userService.update(dto));
    }

    @Test
    void update_ThrowsException_WhenUserNotFound() {
        UserDTO dto = new UserDTO();
        dto.setId(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.update(dto));
    }

    @Test
    void update_ThrowsException_WhenEmailAlreadyExists() {
        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setEmail("taken@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.update(dto));
        assertTrue(ex.getMessage().contains("Email"));
    }

    @Test
    void update_ThrowsException_WhenPhoneAlreadyExists() {
        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setPhone("0999999999");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByPhone("0999999999")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.update(dto));
        assertTrue(ex.getMessage().contains("Số điện thoại"));
    }

    @Test
    void update_Success_UpdatesFieldsAndRoleFromString() {
        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setFullname("New Fullname");
        dto.setEmail("new@example.com");
        dto.setPhone("0902222222");
        dto.setPoints(150);
        dto.setRole("ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("0902222222")).thenReturn(false);

        RoleEntity adminRole = new RoleEntity();
        adminRole.setId(2L);
        adminRole.setName("ADMIN");
        when(roleRepository.findByNameIgnoreCase("ADMIN")).thenReturn(Optional.of(adminRole));

        UserDTO updated = userService.update(dto);

        assertNotNull(updated);
        assertEquals("New Fullname", updated.getFullname());
        assertEquals("new@example.com", updated.getEmail());
        assertEquals("0902222222", updated.getPhone());
        assertEquals(150, updated.getPoints());
        assertEquals("ADMIN", updated.getRole());
        assertFalse(updated.getRoles().isEmpty());
        assertEquals("ADMIN", updated.getRoles().get(0).getName());

        verify(userRoleRepository, times(1)).deleteByUserId(1L);
        verify(userRoleRepository, times(1)).save(any(UserRoleEntity.class));
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void update_Success_UpdatesRoleFromRolesList() {
        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setRoles(List.of(new RoleDTO(3L, "STAFF")));

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        RoleEntity staffRole = new RoleEntity();
        staffRole.setId(3L);
        staffRole.setName("STAFF");
        when(roleRepository.findByNameIgnoreCase("STAFF")).thenReturn(Optional.of(staffRole));

        UserDTO updated = userService.update(dto);

        assertNotNull(updated);
        assertEquals("STAFF", updated.getRole());
        assertEquals(1, updated.getRoles().size());
        assertEquals("STAFF", updated.getRoles().get(0).getName());

        verify(userRoleRepository, times(1)).deleteByUserId(1L);
        verify(userRoleRepository, times(1)).save(any(UserRoleEntity.class));
        verify(userRepository, times(1)).save(existingUser);
    }
}
