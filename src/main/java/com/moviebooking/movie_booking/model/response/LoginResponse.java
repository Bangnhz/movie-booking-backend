package com.moviebooking.movie_booking.model.response;

import com.moviebooking.movie_booking.model.dto.RoleDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LoginResponse {

    private String accessToken;
    private String username;
    private String tokenType;
    List<RoleDTO> roles;
    public LoginResponse(String accessToken, String username, String tokenType) {
        this.accessToken = accessToken;
        this.username = username;
        this.tokenType = tokenType;
    }

    public LoginResponse(String accessToken, String username) {
        this(accessToken, username, "Bearer");
    }
}