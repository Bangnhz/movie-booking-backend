package com.moviebooking.movie_booking.config;

import com.moviebooking.movie_booking.config.security.JwtFilter;
import com.moviebooking.movie_booking.service.impl.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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
        http    .cors(cors -> cors.configure(http))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Cho phép CORS preflight OPTIONS request
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 1. Phân quyền API quản trị (yêu cầu quyền ADMIN):
                        // Ghi phim (POST, PUT, DELETE)
                        .requestMatchers(HttpMethod.POST, "/api/movies/**").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/movies/**").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/movies/**").hasAnyRole("ADMIN")

                        // Ghi rạp (POST, PUT, DELETE)
                        .requestMatchers(HttpMethod.POST, "/api/cinemas/**").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/cinemas/**").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/cinemas/**").hasAnyRole("ADMIN")

                        // Ghi thể loại (POST, PUT, DELETE)
                        .requestMatchers(HttpMethod.POST, "/api/genres/**").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/genres/**").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/genres/**").hasAnyRole("ADMIN")

                        // Ghi phòng (POST, PUT, DELETE)
                        .requestMatchers(HttpMethod.POST, "/api/rooms/**").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/rooms/**").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/rooms/**").hasAnyRole("ADMIN")

                        // Quản trị người dùng (Xóa/Cập nhật người dùng)
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAnyRole("ADMIN")

                        // Quản trị thống kê
                        .requestMatchers("/api/statistics/**").hasAnyRole("ADMIN")

                        // 2. API người dùng hiện tại & đọc lịch sử booking theo userId: yêu cầu đã đăng nhập
                        .requestMatchers("/api/users/me", "/api/users/me/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/users/*/bookings").authenticated()

                        // 3. Các API nghiệp vụ đặt vé, thanh toán: yêu cầu đăng nhập
                        .requestMatchers("/bookings/**", "/api/bookings/**", "/api/payment/vnpay/create", "/payments/**", "/tickets/**").authenticated()

                        // 4. Các endpoint công khai còn lại: permitAll
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
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
