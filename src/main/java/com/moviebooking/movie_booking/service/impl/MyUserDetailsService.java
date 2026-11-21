package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.entity.UserEntity;
import com.moviebooking.movie_booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MyUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Username not found"));

        List<SimpleGrantedAuthority> authorities = user.getUserRoles().stream()
                .flatMap(userRole -> {
                    String roleName = userRole.getRole() != null ? userRole.getRole().getName() : null;
                    if (roleName == null || roleName.isBlank()) {
                        return java.util.stream.Stream.empty();
                    }
                    String upper = roleName.trim().toUpperCase();
                    if (upper.startsWith("ROLE_")) {
                        return java.util.stream.Stream.of(
                                new SimpleGrantedAuthority(upper),
                                new SimpleGrantedAuthority(upper.substring(5))
                        );
                    } else {
                        return java.util.stream.Stream.of(
                                new SimpleGrantedAuthority(upper),
                                new SimpleGrantedAuthority("ROLE_" + upper)
                        );
                    }
                })
                .toList();

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }


}
