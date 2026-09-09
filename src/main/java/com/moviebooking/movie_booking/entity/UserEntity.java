package com.moviebooking.movie_booking.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullname;
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    private String password;

    private Integer points;


    @OneToMany(mappedBy = "user")
    private List<UserRoleEntity> userRoles;
}