package com.moviebooking.movie_booking.model.dto;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String fullname;
    private String username;
    private String email;
    private String phone;
    private Integer points;
    private List<RoleDTO> roles;
}
