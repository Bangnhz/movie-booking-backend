package com.moviebooking.movie_booking.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity
@Table(name = "cinemas")
@Getter
@Setter
public class CinemaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;

    @Column(nullable = false)
    private String email;


    @ManyToOne
    @JoinColumn(name = "city_id",nullable = false)
    private CityEntity city;
}