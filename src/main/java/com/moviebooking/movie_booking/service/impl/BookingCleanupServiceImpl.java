package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.entity.BookingEntity;
import com.moviebooking.movie_booking.entity.SeatStatusEntity;
import com.moviebooking.movie_booking.entity.TicketEntity;
import com.moviebooking.movie_booking.enums.BookingStatus;
import com.moviebooking.movie_booking.enums.SeatStatus;
import com.moviebooking.movie_booking.repository.BookingRepository;
import com.moviebooking.movie_booking.repository.SeatStatusRepository;
import com.moviebooking.movie_booking.repository.TicketRepository;
import com.moviebooking.movie_booking.service.BookingCleanupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingCleanupServiceImpl implements BookingCleanupService {
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private SeatStatusRepository seatStatusRepository;
    @Autowired
    private TicketRepository ticketRepository;

    @Override
    @Scheduled(fixedRate = 60000) //ms
    @Transactional
    public void cleanupExpiredBookings() {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(1);
        List<BookingEntity> bookingEntities = bookingRepository.findAllByStatusAndCreatedAtBefore(BookingStatus.PENDING, fiveMinutesAgo);
        for (BookingEntity booking : bookingEntities) {
            for(TicketEntity ticket : booking.getTickets()){
                SeatStatusEntity seatStatus = seatStatusRepository.findBySeatIdAndShowtimeId(ticket.getSeat().getId(),ticket.getShowtime().getId());
                if(seatStatus != null && seatStatus.getStatus() == SeatStatus.HELD){
                    seatStatus.setStatus(SeatStatus.AVAILABLE);
                    seatStatus.setHoldExpiresAt(null);
                    seatStatusRepository.save(seatStatus);
                }
            }
//            ticketRepository.deleteAll(booking.getTickets());
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            System.out.println("--- Tự động xóa ---");
        }
    }
}
