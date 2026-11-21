package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.entity.RoleEntity;
import com.moviebooking.movie_booking.entity.UserEntity;
import com.moviebooking.movie_booking.entity.UserRoleEntity;
import com.moviebooking.movie_booking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyUserDetailsServiceTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MyUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_MapsBothRawAndPrefixedRoles() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("testadmin");
        user.setPassword("hashedpass");

        RoleEntity role = new RoleEntity();
        role.setName("ADMIN");

        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setRole(role);
        userRole.setUser(user);

        user.setUserRoles(List.of(userRole));

        when(userRepository.findByUsername("testadmin")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("testadmin");

        assertNotNull(userDetails);
        assertEquals("testadmin", userDetails.getUsername());

        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        assertTrue(authorities.contains("ADMIN"));
        assertTrue(authorities.contains("ROLE_ADMIN"));
    }

    @Test
    void loadUserByUsername_ThrowsException_WhenNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("unknown")
        );
    }
}
