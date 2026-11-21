package com.moviebooking.movie_booking.repository.customer.impl;

import com.moviebooking.movie_booking.model.request.StatisticRequest;
import com.moviebooking.movie_booking.repository.customer.StatisticRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class StatisticRepositoryImpl implements StatisticRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Object[]> getStatistics(StatisticRequest request) {

        StringBuilder sql = new StringBuilder("""
        SELECT
            (
                SELECT COUNT(DISTINCT m.id)
                FROM movies m
                JOIN showtimes st ON st.movie_id = m.id
                JOIN rooms r ON r.id = st.room_id
                WHERE 1 = 1
        """);

        Map<String, Object> params = new HashMap<>();

        if (request.getYear() != null) {
            sql.append(" AND YEAR(st.start_time) = :year");
            params.put("year", request.getYear());
        }

        if (request.getMonth() != null) {
            sql.append(" AND MONTH(st.start_time) = :month");
            params.put("month", request.getMonth());
        }

        if (request.getMovieId() != null) {
            sql.append(" AND m.id = :movieId");
            params.put("movieId", request.getMovieId());
        }

        if (request.getCinemaId() != null) {
            sql.append(" AND r.cinema_id = :cinemaId");
            params.put("cinemaId", request.getCinemaId());
        }

        sql.append("""
            ) AS totalMovies,

            (
                SELECT COUNT(t.id)
                FROM tickets t
                JOIN bookings b ON b.id = t.booking_id
                JOIN showtimes st ON st.id = t.showtime_id
                JOIN rooms r ON r.id = st.room_id
                WHERE b.status = 'PAID'
        """);

        if (request.getYear() != null) {
            sql.append(" AND YEAR(b.created_at) = :year");
        }

        if (request.getMonth() != null) {
            sql.append(" AND MONTH(b.created_at) = :month");
        }

        if (request.getMovieId() != null) {
            sql.append(" AND st.movie_id = :movieId");
        }

        if (request.getCinemaId() != null) {
            sql.append(" AND r.cinema_id = :cinemaId");
        }

        sql.append("""
            ) AS soldTickets,

            (
                SELECT COALESCE(SUM(t.final_price), 0)
                FROM tickets t
                JOIN bookings b ON b.id = t.booking_id
                JOIN showtimes st ON st.id = t.showtime_id
                JOIN rooms r ON r.id = st.room_id
                WHERE b.status = 'PAID'
        """);

        if (request.getYear() != null) {
            sql.append(" AND YEAR(b.created_at) = :year");
        }

        if (request.getMonth() != null) {
            sql.append(" AND MONTH(b.created_at) = :month");
        }

        if (request.getMovieId() != null) {
            sql.append(" AND st.movie_id = :movieId");
        }

        if (request.getCinemaId() != null) {
            sql.append(" AND r.cinema_id = :cinemaId");
        }

        sql.append("""
            ) AS revenue,

            (
                SELECT COALESCE(
                    ROUND(
                        (seatStatistic.totalSeats - seatStatistic.soldSeats) * 100.0 / NULLIF(seatStatistic.totalSeats, 0),
                        2
                    ),
                    0.0
                )
                FROM
                (
                    SELECT

                        (
                            SELECT COUNT(*)
                            FROM showtimes st
                            JOIN rooms r ON st.room_id = r.id
                            JOIN seats s ON s.room_id = r.id
                            WHERE 1 = 1
        """);

        if (request.getYear() != null) {
            sql.append(" AND YEAR(st.start_time) = :year");
        }

        if (request.getMonth() != null) {
            sql.append(" AND MONTH(st.start_time) = :month");
        }

        if (request.getMovieId() != null) {
            sql.append(" AND st.movie_id = :movieId");
        }

        if (request.getCinemaId() != null) {
            sql.append(" AND r.cinema_id = :cinemaId");
        }

        sql.append("""
                        ) AS totalSeats,

                        (
                            SELECT COUNT(*)
                            FROM tickets t
                            JOIN bookings b ON b.id = t.booking_id
                            JOIN showtimes st ON st.id = t.showtime_id
                            JOIN rooms r ON r.id = st.room_id
                            WHERE b.status = 'PAID'
        """);

        if (request.getYear() != null) {
            sql.append(" AND YEAR(st.start_time) = :year");
        }

        if (request.getMonth() != null) {
            sql.append(" AND MONTH(st.start_time) = :month");
        }

        if (request.getMovieId() != null) {
            sql.append(" AND st.movie_id = :movieId");
        }

        if (request.getCinemaId() != null) {
            sql.append(" AND r.cinema_id = :cinemaId");
        }

        sql.append("""
                        ) AS soldSeats
                ) seatStatistic
            ) AS averageEmptySeatRate
        """);

        Query query = entityManager.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);

        return query.getResultList();
    }

    @Override
    public List<Object[]> getChartData(StatisticRequest request) {
        boolean isMonth = request.getMonth() != null;
        String periodExpr = isMonth ? "DAY(b.created_at)" : "MONTH(b.created_at)";

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
           .append(periodExpr).append(" AS period, ")
           .append("COUNT(t.id) AS tickets, ")
           .append("COALESCE(SUM(t.final_price), 0) AS revenue ")
           .append("FROM tickets t ")
           .append("JOIN bookings b ON b.id = t.booking_id ")
           .append("JOIN showtimes st ON st.id = t.showtime_id ")
           .append("JOIN rooms r ON r.id = st.room_id ")
           .append("WHERE b.status = 'PAID' ");

        Map<String, Object> params = new HashMap<>();

        if (request.getYear() != null) {
            sql.append("AND YEAR(b.created_at) = :year ");
            params.put("year", request.getYear());
        }

        if (request.getMonth() != null) {
            sql.append("AND MONTH(b.created_at) = :month ");
            params.put("month", request.getMonth());
        }

        if (request.getMovieId() != null) {
            sql.append("AND st.movie_id = :movieId ");
            params.put("movieId", request.getMovieId());
        }

        if (request.getCinemaId() != null) {
            sql.append("AND r.cinema_id = :cinemaId ");
            params.put("cinemaId", request.getCinemaId());
        }

        sql.append("GROUP BY ").append(periodExpr).append(" ")
           .append("ORDER BY ").append(periodExpr).append(" ASC");

        Query query = entityManager.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);

        return query.getResultList();
    }
}
