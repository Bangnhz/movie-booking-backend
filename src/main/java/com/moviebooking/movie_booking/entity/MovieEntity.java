package com.moviebooking.movie_booking.entity;

import com.moviebooking.movie_booking.enums.AgeRating;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "movies")
@Getter
@Setter
public class MovieEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "poster_url")
    private String posterUrl;

    @Column(name = "is_active")
    private boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_rating")
    private AgeRating ageRating;

    private Integer duration;

    @OneToMany(mappedBy = "movie")
    private List<ShowtimeEntity> showtimes;

    @OneToMany(mappedBy = "movie")
    private List<MovieGenreEntity> movieGenres;
}
