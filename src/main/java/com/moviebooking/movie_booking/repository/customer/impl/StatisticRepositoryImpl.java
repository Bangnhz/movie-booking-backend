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

        sql.append("""
            ) AS totalMovies,

            (
                SELECT COUNT(*)
                FROM tickets t
                JOIN bookings b ON b.id = t.booking_id
                WHERE b.status = 'PAID'
        """);

        if (request.getYear() != null) {
            sql.append(" AND YEAR(b.created_at) = :year");
        }

        if (request.getMonth() != null) {
            sql.append(" AND MONTH(b.created_at) = :month");
        }

        sql.append("""
            ) AS soldTickets,

            (
                SELECT COALESCE(SUM(b.total_price),0)
                FROM bookings b
                WHERE b.status = 'PAID'
        """);

        if (request.getYear() != null) {
            sql.append(" AND YEAR(b.created_at) = :year");
        }

        if (request.getMonth() != null) {
            sql.append(" AND MONTH(b.created_at) = :month");
        }

        sql.append("""
            ) AS revenue,

            (
                SELECT ROUND(
                    (
                        totalSeats - soldSeats
                    ) * 100.0 / totalSeats,
                    2
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

        sql.append("""
                        ) AS totalSeats,

                        (
                            SELECT COUNT(*)
                            FROM tickets t
                            JOIN bookings b ON b.id = t.booking_id
                            JOIN showtimes st ON st.id = t.showtime_id
                            WHERE b.status = 'PAID'
        """);

        if (request.getYear() != null) {
            sql.append(" AND YEAR(st.start_time) = :year");
        }

        if (request.getMonth() != null) {
            sql.append(" AND MONTH(st.start_time) = :month");
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
    /*
    SELECT
    (
        SELECT COUNT(DISTINCT m.id)
        FROM movies m
        JOIN showtimes st ON st.movie_id = m.id
        WHERE 1 = 1
          AND YEAR(st.start_time) = :year
          AND MONTH(st.start_time) = :month
    ) AS totalMovies,

    (
        SELECT COUNT(*)
        FROM tickets t
        JOIN bookings b ON b.id = t.booking_id
        WHERE b.status = 'PAID'
          AND YEAR(b.created_at) = :year
          AND MONTH(b.created_at) = :month
    ) AS soldTickets,

    (
        SELECT COALESCE(SUM(b.total_price), 0)
        FROM bookings b
        WHERE b.status = 'PAID'
          AND YEAR(b.created_at) = :year
          AND MONTH(b.created_at) = :month
    ) AS revenue,

    (
        SELECT ROUND(
            (seatStatistic.totalSeats - seatStatistic.soldSeats)
            * 100.0 / seatStatistic.totalSeats,
            2
        )
        FROM (
            SELECT
                (
                    SELECT COUNT(*)
                    FROM showtimes st
                    JOIN rooms r ON st.room_id = r.id
                    JOIN seats s ON s.room_id = r.id
                    WHERE YEAR(st.start_time) = :year
                      AND MONTH(st.start_time) = :month
                ) AS totalSeats,

                (
                    SELECT COUNT(*)
                    FROM tickets t
                    JOIN bookings b ON b.id = t.booking_id
                    JOIN showtimes st ON st.id = t.showtime_id
                    WHERE b.status = 'PAID'
                      AND YEAR(st.start_time) = :year
                      AND MONTH(st.start_time) = :month
                ) AS soldSeats
        ) AS seatStatistic
    ) AS averageEmptySeatRate;

     */
}
