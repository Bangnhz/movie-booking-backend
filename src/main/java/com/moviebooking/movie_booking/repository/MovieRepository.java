package com.moviebooking.movie_booking.repository;

import com.moviebooking.movie_booking.entity.MovieEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<MovieEntity,Long> {
//    🎬 Coming Soon
//    m.release_date > NOW()
//   🎬 Now Showing
//    m.release_date <= NOW()
//    AND EXISTS showtime tương lai
//    🎬 Ended
//    NOT EXISTS showtime tương lai

    @Query(value="""
        SELECT * FROM movies m
        WHERE EXISTS (
            SELECT 1
            FROM showtimes s
            WHERE m.id = s.movie_id AND s.start_time >= NOW()
        )
        AND m.is_active = TRUE 
        AND m.release_date <= NOW()
        """,nativeQuery=true)
    List<MovieEntity> findAllNowShowing();

    @Query(value = "SELECT * FROM movies WHERE release_date > CURRENT_DATE()",nativeQuery = true)
    List<MovieEntity> findAllComingSoon();

    @Query("""
        SELECT m FROM MovieEntity m
        WHERE NOT EXISTS (
            SELECT 1 FROM ShowtimeEntity s
            WHERE s.movie.id = m.id
            AND s.startTime >= CURRENT_TIMESTAMP
        )
    """)
    List<MovieEntity> findAllEndedMovies();
    Page<MovieEntity> findAll(Pageable pageable);
}
