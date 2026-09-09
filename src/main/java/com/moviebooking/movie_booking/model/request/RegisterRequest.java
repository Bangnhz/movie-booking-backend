package com.moviebooking.movie_booking.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String fullname;
    private String username;
    private String email;
    private String phone;
    private String password;
}