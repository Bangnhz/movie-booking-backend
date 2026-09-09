package com.moviebooking.movie_booking.repository.customer.impl;

import com.moviebooking.movie_booking.model.request.search.BookingSearchRequest;
import com.moviebooking.movie_booking.repository.customer.BookingRepositoryCustomer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class BookingRepositoryImpl implements BookingRepositoryCustomer {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Object[]> findByUserId(Long userId, BookingSearchRequest bookingSearchRequest, Pageable pageable) {
        StringBuilder sql = new StringBuilder("""
        SELECT
            -- 1. Booking (0 -> 3)
            b.id                    AS booking_id,
            b.created_at,
            b.status,
            b.total_price,
        
            -- 2. Movie (4 -> 7)
            m.id                    AS movie_id,
            m.title,
            m.poster_url,
            m.age_rating,
        
            -- 3. Cinema (8 -> 9)
            c.id                    AS cinema_id,
            c.name                  AS cinema_name,
        
            -- 4. Room (10 -> 11)
            r.id                    AS room_id,
            r.name                  AS room_name,
        
            -- 5. Showtime (12 -> 14)
            st.id                   AS showtime_id,
            st.start_time,
            st.end_time,
        
            -- 6. Seat & Seat Type cho SeatDTO (15 -> 19)
            s.id                    AS seat_id,
            s.row_index,
            s.column_index,
            stt.name                AS seat_type_name,
            stt.surcharge           AS seat_surcharge
            
        FROM bookings b
        INNER JOIN tickets t ON t.booking_id = b.id
        INNER JOIN seats s ON s.id = t.seat_id
        INNER JOIN seat_types stt ON s.seat_type_id = stt.id  -- JOIN thêm để lấy thông tin SeatDTO
        INNER JOIN rooms r ON r.id = s.room_id
        INNER JOIN cinemas c ON c.id = r.cinema_id
        INNER JOIN showtimes st ON st.id = t.showtime_id
        INNER JOIN movies m ON m.id = st.movie_id
        """);

        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        Map<String, Object> params = new HashMap<>();

        if (userId != null) {
            where.append(" AND b.user_id = :userId ");
            params.put("userId", userId);
        }

        if (bookingSearchRequest.getCinemaName() != null && !bookingSearchRequest.getCinemaName().trim().isEmpty()) {
            where.append(" AND c.name LIKE :cinemaName ");
            params.put("cinemaName", "%" + bookingSearchRequest.getCinemaName().trim() + "%");
        }
        if (bookingSearchRequest.getFromDate() != null) {
            where.append(" AND DATE(b.created_at) >= :fromDate ");
            params.put("fromDate", bookingSearchRequest.getFromDate());
        }
        if (bookingSearchRequest.getToDate() != null) {
            where.append(" AND DATE(b.created_at) <= :toDate ");
            params.put("toDate", bookingSearchRequest.getToDate());
        }
        if (bookingSearchRequest.getStatus() != null) {
            where.append(" AND b.status = :status ");
            params.put("status", bookingSearchRequest.getStatus().name());
        }

        sql.append(where);
        sql.append(" ORDER BY b.created_at DESC ");

        if (pageable != null && pageable.isPaged()) {
            sql.append(" LIMIT :limit OFFSET :offset ");
            params.put("limit", pageable.getPageSize());
            params.put("offset", pageable.getOffset());
        }

        Query query = entityManager.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);

        return query.getResultList();
    }
    @Override
    public Long countTotal(Long userId, BookingSearchRequest bookingSearchRequest) {
        // Sử dụng COUNT(DISTINCT b.id) để đếm đúng số đơn hàng, không bị trùng lặp do kết quả có nhiều ghế
        StringBuilder sql = new StringBuilder("""
        SELECT COUNT(DISTINCT b.id)
        FROM bookings b
        INNER JOIN tickets t ON t.booking_id = b.id
        INNER JOIN seats s ON s.id = t.seat_id
        INNER JOIN seat_types stt ON s.seat_type_id = stt.id
        INNER JOIN rooms r ON r.id = s.room_id
        INNER JOIN cinemas c ON c.id = r.cinema_id
        INNER JOIN showtimes st ON st.id = t.showtime_id
        INNER JOIN movies m ON m.id = st.movie_id
        """);

        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        Map<String, Object> params = new HashMap<>();

        if (userId != null) {
            where.append(" AND b.user_id = :userId ");
            params.put("userId", userId);
        }

        if (bookingSearchRequest.getCinemaName() != null && !bookingSearchRequest.getCinemaName().trim().isEmpty()) {
            where.append(" AND c.name LIKE :cinemaName ");
            params.put("cinemaName", "%" + bookingSearchRequest.getCinemaName().trim() + "%");
        }

        if (bookingSearchRequest.getFromDate() != null) {
            where.append(" AND DATE(b.created_at) >= :fromDate ");
            params.put("fromDate", bookingSearchRequest.getFromDate());
        }

        if (bookingSearchRequest.getToDate() != null) {
            where.append(" AND DATE(b.created_at) <= :toDate ");
            params.put("toDate", bookingSearchRequest.getToDate());
        }

        if (bookingSearchRequest.getStatus() != null) {
            where.append(" AND b.status = :status ");
            params.put("status", bookingSearchRequest.getStatus().name());
        }

        sql.append(where);

        Query query = entityManager.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);

        return ((Number) query.getSingleResult()).longValue();
    }
}