package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.entity.BookingEntity;
import com.moviebooking.movie_booking.entity.PaymentEntity;
import com.moviebooking.movie_booking.entity.SeatStatusEntity;
import com.moviebooking.movie_booking.entity.TicketEntity;
import com.moviebooking.movie_booking.enums.BookingStatus;
import com.moviebooking.movie_booking.enums.PaymentStatus;
import com.moviebooking.movie_booking.enums.SeatStatus;
import com.moviebooking.movie_booking.model.dto.VNPayQueryResult;
import com.moviebooking.movie_booking.repository.BookingRepository;
import com.moviebooking.movie_booking.repository.PaymentRepository;
import com.moviebooking.movie_booking.repository.SeatStatusRepository;
import com.moviebooking.movie_booking.repository.ShowtimeRepository;
import com.moviebooking.movie_booking.service.BookingCleanupService;
import com.moviebooking.movie_booking.service.BookingService;
import com.moviebooking.movie_booking.service.RedisService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingCleanupServiceImpl implements BookingCleanupService {
    private static final Logger log = LoggerFactory.getLogger(BookingCleanupServiceImpl.class);

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private SeatStatusRepository seatStatusRepository;
    @Autowired
    private ShowtimeRepository showtimeRepository;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private VNPayService vnPayService;
    @Autowired
    private RedisService redisService;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Scheduled(fixedRate = 60000) // Mỗi 1 phút quét 1 lần
    @Transactional
    public void cleanupExpiredBookings() {
        LocalDateTime expiredBefore = LocalDateTime.now().minusMinutes(15);
        // Hạn tối đa giữ ghế nếu giao dịch thanh toán vẫn đang PENDING (20 phút = 15 phút đặt + 5 phút đệm IPN/QueryDR)
        LocalDateTime maxPendingLimit = LocalDateTime.now().minusMinutes(20);

        List<BookingEntity> bookingEntities = bookingRepository.findAllByStatusAndCreatedAtBefore(BookingStatus.PENDING, expiredBefore);
        bookingEntities.sort(java.util.Comparator.comparing(booking -> booking.getShowtime().getId()));

        for (BookingEntity booking : bookingEntities) {
            showtimeRepository.findByIdForUpdate(booking.getShowtime().getId())
                    .orElseThrow(() -> new IllegalStateException("Không tìm thấy showtime"));
            entityManager.refresh(booking);

            if (booking.getStatus() != BookingStatus.PENDING
                    || !booking.getCreatedAt().isBefore(expiredBefore)) {
                continue;
            }

            // 1. Kiểm tra xem booking có giao dịch thanh toán nào đang PENDING không
            Optional<PaymentEntity> pendingPaymentOpt = paymentRepository
                    .findTopByBooking_IdAndStatusOrderByCreatedAtDesc(booking.getId(), PaymentStatus.PENDING);

            if (pendingPaymentOpt.isEmpty()) {
                // Không có giao dịch thanh toán nào đang chờ (khách chưa ấn thanh toán hoặc thanh toán trước đó đã FAILED)
                // Đã quá 15 phút -> Hủy booking và nhả ghế ngay
                log.info("Booking ID {} không có giao dịch PENDING và đã quá 15 phút. Tiến hành hủy và nhả ghế.", booking.getId());
                cancelBookingAndReleaseSeats(booking);
                continue;
            }

            // 2. Booking CÓ giao dịch thanh toán đang PENDING:
            // KHÔNG nhả ghế ngay tại phút 15 vì khách có thể vừa trả tiền xong trên VNPay và IPN đang trên đường tới!
            PaymentEntity pendingPayment = pendingPaymentOpt.get();

            // Chủ động tra cứu trạng thái giao dịch với VNPay qua API QueryDR
            VNPayQueryResult queryResult = vnPayService.queryTransaction(
                    pendingPayment.getTransactionCode(),
                    pendingPayment.getCreatedAt()
            );

            if (queryResult.isSuccess()) {
                // VNPay xác nhận đã thanh toán THÀNH CÔNG!
                log.info("QueryDR xác nhận giao dịch {} của booking {} đã thanh toán THÀNH CÔNG. Cập nhật vé...",
                        pendingPayment.getTransactionCode(), booking.getId());
                bookingService.processPaymentResult(pendingPayment.getTransactionCode(), true);
            } else if (queryResult.isFailed()) {
                // VNPay xác nhận giao dịch THẤT BẠI hoặc KHÔNG TỒN TẠI
                log.info("QueryDR xác nhận giao dịch {} của booking {} THẤT BẠI hoặc KHÔNG TỒN TẠI (code={}, status={}). Hủy booking và nhả ghế...",
                        pendingPayment.getTransactionCode(), booking.getId(), queryResult.getResponseCode(), queryResult.getTransactionStatus());
                bookingService.processPaymentResult(pendingPayment.getTransactionCode(), false);
                cancelBookingAndReleaseSeats(booking);
            } else {
                // Trạng thái PENDING hoặc UNKNOWN (chưa hoàn tất hoặc không kết nối được tới VNPay)
                if (booking.getCreatedAt().isAfter(maxPendingLimit)) {
                    // Vẫn trong khoảng thời gian đệm (từ phút 15 đến phút 20): TIẾP TỤC GIỮ GHẾ, chờ IPN hoặc lần quét sau
                    log.info("Giao dịch {} của booking {} vẫn đang chờ xử lý trên VNPay. Tiếp tục giữ ghế chờ xác nhận...",
                            pendingPayment.getTransactionCode(), booking.getId());
                } else {
                    // Đã quá 20 phút mà vẫn không có xác nhận thành công -> Hết thời gian ân hạn, buộc phải hủy và nhả ghế
                    log.warn("Giao dịch {} của booking {} đã quá 20 phút mà không có xác nhận thành công. Hủy giao dịch, hủy booking và nhả ghế.",
                            pendingPayment.getTransactionCode(), booking.getId());
                    bookingService.processPaymentResult(pendingPayment.getTransactionCode(), false);
                    cancelBookingAndReleaseSeats(booking);
                }
            }
        }
    }

    private void cancelBookingAndReleaseSeats(BookingEntity booking) {
        for (TicketEntity ticket : booking.getTickets()) {
            SeatStatusEntity seatStatus = seatStatusRepository.findBySeatIdAndShowtimeId(
                    ticket.getSeat().getId(), ticket.getShowtime().getId());
            if (seatStatus != null && seatStatus.getStatus() == SeatStatus.HELD) {
                seatStatus.setStatus(SeatStatus.AVAILABLE);
                seatStatus.setHoldExpiresAt(null);
                seatStatusRepository.save(seatStatus);
            }
        }
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Nhả khóa ghế trên Redis nếu còn lưu
        List<Long> seatIds = booking.getTickets().stream()
                .map(t -> t.getSeat().getId())
                .toList();
        Long userId = (booking.getUser() != null) ? booking.getUser().getId() : null;
        if (redisService != null && userId != null) {
            redisService.releaseSeats(booking.getShowtime().getId(), seatIds, userId);
        }
    }
}
