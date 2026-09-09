package com.moviebooking.movie_booking.config;

import com.moviebooking.movie_booking.config.security.JwtFilter;
import com.moviebooking.movie_booking.service.impl.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private JwtFilter jwtFilter;
    @Autowired
    private MyUserDetailsService userDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http    .cors(cors -> cors.configure(http)) // 👈 Thêm dòng này để kích hoạt CORS
                .csrf(csrf -> csrf.disable())
                .csrf(csrf -> csrf.disable()) // Vô hiệu hóa CSRF cho API
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ) // Không sử dụng Session
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/css/**","/js/**","/images/**").permitAll() // Endpoint đăng ký/đăng nhập
//                        .requestMatchers("/**").permitAll()
//                        .requestMatchers("/bookings/**","/payments/**").authenticated()// Các API công khai
//                        .anyRequest().authenticated() // Còn lại phải đăng nhập
                            .requestMatchers("/bookings/**","/payments/**","/tickets/**").authenticated()
                            .anyRequest().permitAll()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class); // 👈 GẮN Ở ĐÂY;
        return http.build();
    }
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }
  }
