package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.converter.MovieConverter;
import com.moviebooking.movie_booking.converter.SeatConverter;
import com.moviebooking.movie_booking.entity.*;
import com.moviebooking.movie_booking.enums.BookingStatus;
import com.moviebooking.movie_booking.enums.SeatStatus;
import com.moviebooking.movie_booking.model.TempBookingRequest;
import com.moviebooking.movie_booking.model.dto.ShowtimeSeatDTO;
import com.moviebooking.movie_booking.model.request.BookingRequest;
import com.moviebooking.movie_booking.model.request.search.BookingSearchRequest;
import com.moviebooking.movie_booking.model.response.BookingResponse;
import com.moviebooking.movie_booking.model.response.BookingSearchResponse;
import com.moviebooking.movie_booking.repository.*;
import com.moviebooking.movie_booking.service.BookingService;
import com.moviebooking.movie_booking.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.moviebooking.movie_booking.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BookingServiceImpl implements BookingService {
    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ShowtimeRepository showtimeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private SeatStatusRepository seatStatusRepository;
    @Autowired
    private SeatConverter seatConverter;
    @Autowired
    private MovieConverter movieConverter;
    @Autowired
    private RedisService redisService;
    @Autowired
    private PaymentRepository paymentRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final int HOLD_MINUTES = 15;

    @Override
    @Transactional
    public Long create(BookingRequest request, String username) {
        validateRequest(request);
        ShowtimeEntity showtime = getShowtime(request.getShowtimeId());
        UserEntity user = getUserByUsername(username);
        List<SeatEntity> seats = validateSeats(request, showtime);
        if (!redisService.reserveSeats(showtime.getId(), request.getSeatIds(), user.getId(), HOLD_MINUTES)) {
            throw new IllegalStateException("Ghế đang được người khác giữ!");
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) {
                        redisService.releaseSeats(showtime.getId(), request.getSeatIds(), user.getId());
                    }
                }
            });
        }

        BookingEntity booking = new BookingEntity();
        booking.setUser(user);
        booking.setShowtime(showtime);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setStatus(BookingStatus.PENDING);
        List<TicketEntity> tickets = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (SeatEntity seat : seats) {
            SeatStatusEntity status = seatStatusRepository.findBySeatIdAndShowtimeId(seat.getId(), showtime.getId());
            if (status == null) {
                status = new SeatStatusEntity();
                status.setSeat(seat);
                status.setShowtime(showtime);
            }
            status.setStatus(SeatStatus.HELD);
            status.setHoldExpiresAt(booking.getCreatedAt().plusMinutes(HOLD_MINUTES));
            seatStatusRepository.save(status);

            BigDecimal price = showtime.getBasePrice().add(seat.getSeatType().getSurcharge());
            TicketEntity ticket = new TicketEntity();
            ticket.setBooking(booking);
            ticket.setShowtime(showtime);
            ticket.setSeat(seat);
            ticket.setOriginalPrice(price);
            ticket.setDiscountAmount(BigDecimal.ZERO);
            ticket.setFinalPrice(price);
            tickets.add(ticket);
            total = total.add(price);
        }
        booking.setTickets(tickets);
        booking.setTotalPrice(total);
        return bookingRepository.saveAndFlush(booking).getId();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        BookingEntity booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy booking id=" + id));
        showtimeRepository.findByIdForUpdate(booking.getShowtime().getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy showtime"));
        // Reload after obtaining the same lock used by booking creation and cleanup.
        entityManager.refresh(booking);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return;
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể hủy booking đang chờ thanh toán");
        }
        cancelPendingBooking(booking);
    }

    private void cancelPendingBooking(BookingEntity booking) {
        List<Long> seatIds = booking.getTickets().stream().map(ticket -> ticket.getSeat().getId()).toList();
        for (TicketEntity ticket : booking.getTickets()) {
            SeatStatusEntity status = seatStatusRepository.findBySeatIdAndShowtimeId(
                    ticket.getSeat().getId(), booking.getShowtime().getId());
            if (status != null && status.getStatus() == SeatStatus.HELD) {
                status.setStatus(SeatStatus.AVAILABLE);
                status.setHoldExpiresAt(null);
                seatStatusRepository.save(status);
            }
        }
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.saveAndFlush(booking);

        // Hủy toàn bộ giao dịch thanh toán PENDING của booking này để ngăn IPN success
        // muộn chuyển booking thành PAID
        List<PaymentEntity> pendingPayments = paymentRepository.findAllByBooking_IdAndStatus(
                booking.getId(), PaymentStatus.PENDING);
        for (PaymentEntity payment : pendingPayments) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    redisService.releaseSeats(booking.getShowtime().getId(), seatIds, booking.getUser().getId());
                }
            });
        }
    }

    @Override
    @Transactional
    public String holdBooking(BookingRequest request, Long userId) {
        validateRequest(request);
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Thông tin user không hợp lệ");
        }
        ShowtimeEntity showtime = getShowtime(request.getShowtimeId());
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Không tìm thấy user");
        }
        validateSeats(request, showtime);
        request.setCreatedAt(LocalDateTime.now());
        if (!redisService.holdSeats(showtime.getId(), request.getSeatIds(), userId, HOLD_MINUTES)) {
            throw new IllegalStateException("Ghế đang được người khác thanh toán!");
        }
        try {
            return redisService.saveTempBooking(request, userId, HOLD_MINUTES);
        } catch (RuntimeException exception) {
            redisService.releaseSeats(showtime.getId(), request.getSeatIds(), userId);
            throw exception;
        }
    }

    private UserEntity getUserByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Không tìm thấy user");
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));
    }

    private void validateRequest(BookingRequest request) {
        if (request == null || request.getShowtimeId() == null || request.getShowtimeId() <= 0
                || request.getSeatIds() == null || request.getSeatIds().isEmpty()
                || request.getSeatIds().stream().anyMatch(id -> id == null || id <= 0)
                || request.getSeatIds().stream().distinct().count() != request.getSeatIds().size()) {
            throw new IllegalArgumentException("Thông tin đặt vé không hợp lệ hoặc danh sách ghế bị trùng");
        }
    }

    private ShowtimeEntity getShowtime(Long id) {
        ShowtimeEntity showtime = showtimeRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy showtime"));
        if (showtime.getStartTime() == null || !showtime.getStartTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Suất chiếu đã bắt đầu hoặc hết hạn đặt vé");
        }
        return showtime;
    }

    private List<SeatEntity> validateSeats(BookingRequest request, ShowtimeEntity showtime) {
        List<SeatEntity> seats = new ArrayList<>();
        for (Long seatId : request.getSeatIds()) {
            SeatEntity seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ghế id=" + seatId));
            if (!seat.getRoom().getId().equals(showtime.getRoom().getId())) {
                throw new IllegalArgumentException("Ghế không thuộc phòng của suất chiếu");
            }
            if (ticketRepository.existsByShowtimeIdAndSeatIdAndBooking_StatusIn(showtime.getId(), seatId,
                    new BookingStatus[] { BookingStatus.PENDING, BookingStatus.PAID })) {
                throw new IllegalStateException("Ghế đã có người đặt");
            }
            SeatStatusEntity status = seatStatusRepository.findBySeatIdAndShowtimeId(seatId, showtime.getId());
            if (status != null && (status.getStatus() == SeatStatus.BOOKED
                    || (status.getStatus() == SeatStatus.HELD && (status.getHoldExpiresAt() == null
                            || status.getHoldExpiresAt().isAfter(LocalDateTime.now()))))) {
                throw new IllegalStateException("Ghế đang được đặt");
            }
            seats.add(seat);
        }
        return seats;
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getTempBookingResponse(String uuid, Long userId) {
        TempBookingRequest tempBooking = redisService.getTempBooking(uuid);
        if (tempBooking == null || userId == null || !userId.equals(tempBooking.getUserId())) {
            throw new AccessDeniedException("Không có quyền truy cập phiên đặt vé này");
        }
        BookingRequest request = tempBooking.getBookingRequest();
        if (request == null || request.getCreatedAt() == null
                || !request.getCreatedAt().plusMinutes(HOLD_MINUTES).isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Phiên đặt vé đã hết hạn! Vui lòng đặt lại");
        }
        ShowtimeEntity showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy showtime"));
        List<SeatEntity> seats = seatRepository.findAllById(request.getSeatIds());
        BookingResponse response = new BookingResponse();
        response.setId(uuid);
        response.setStatus(BookingStatus.PENDING);
        response.setCreatedAt(request.getCreatedAt());
        response.setExpirationTime(request.getCreatedAt().plusMinutes(HOLD_MINUTES));
        response.setCinemaName(showtime.getRoom().getCinema().getName());
        response.setRoomName(showtime.getRoom().getName());
        response.setStartTime(showtime.getStartTime());
        response.setMovieBookingDTO(movieConverter.toMovieBookingDTO(showtime.getMovie()));
        response.setSelectedSeats(seats.stream().map(seat -> {
            ShowtimeSeatDTO dto = seatConverter.toSeatDTO(seat, showtime.getId());
            dto.setStatus(SeatStatus.HELD);
            return dto;
        }).toList());
        response.setTotalPrice(seats.stream().map(seat -> showtime.getBasePrice()
                .add(seat.getSeatType().getSurcharge())).reduce(BigDecimal.ZERO, BigDecimal::add));
        return response;
    }

    @Override
    public Page<BookingSearchResponse> getBookingsByUserId(Long userId, BookingSearchRequest request,
            Pageable pageable) {
        List<Object[]> results = bookingRepository.findByUserId(userId, request, pageable);

        // Dùng LinkedHashMap giữ nguyên thứ tự tin cậy từ SQL
        Map<Integer, BookingSearchResponse> bookingMap = new java.util.LinkedHashMap<>();

        for (Object[] row : results) {
            Integer bookingId = (Integer) row[0];

            BookingSearchResponse response = bookingMap.get(bookingId);
            if (response == null) {
                response = new BookingSearchResponse();

                // 1. Map Đơn hàng (Booking)
                response.setBookingId(bookingId);
                if (row[1] != null) {
                    response.setBookingDate(((java.sql.Timestamp) row[1]).toLocalDateTime());
                }
                response.setStatus(com.moviebooking.movie_booking.enums.BookingStatus.valueOf((String) row[2]));
                response.setTotalAmount((java.math.BigDecimal) row[3]);

                // 2. Map Phim (Movie)
                response.setMovieId((Integer) row[4]);
                response.setMovieTitle((String) row[5]);
                response.setPosterUrl((String) row[6]);
                response.setAgeRating((String) row[7]);

                // 3. Map Rạp & Phòng (Cinema & Room)
                response.setCinemaId((Integer) row[8]);
                response.setCinemaName((String) row[9]);
                response.setRoomId((Integer) row[10]);
                response.setRoomName((String) row[11]);

                // 4. Map Suất chiếu (Showtime)
                response.setShowtimeId((Integer) row[12]);
                if (row[13] != null) {
                    response.setStartTime(((java.sql.Timestamp) row[13]).toLocalDateTime());
                }
                if (row[14] != null) {
                    response.setEndTime(((java.sql.Timestamp) row[14]).toLocalDateTime());
                }

                response.setSeats(new java.util.ArrayList<>());

                bookingMap.put(bookingId, response);
            }

            // 5. Map thông tin Ghế vào ShowtimeSeatDTO (Index 15 -> 19)
            if (row[15] != null) {
                ShowtimeSeatDTO showtimeSeatDto = new ShowtimeSeatDTO();

                showtimeSeatDto.setId(((Number) row[15]).longValue());
                showtimeSeatDto.setRowIndex((String) row[16]);
                showtimeSeatDto.setColumnIndex(((Number) row[17]).intValue());
                showtimeSeatDto.setType((String) row[18]);
                showtimeSeatDto.setSurcharge((java.math.BigDecimal) row[19]);
                showtimeSeatDto.setStatus(com.moviebooking.movie_booking.enums.SeatStatus.BOOKED);

                response.getSeats().add(showtimeSeatDto);
            }
        }
        List<BookingSearchResponse> content = new java.util.ArrayList<>(bookingMap.values());
        Long total = bookingRepository.countTotal(userId, request);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    @Transactional
    public Long createPendingFromTemp(String tempBookingId, Long userId) {
        if (tempBookingId == null || tempBookingId.isBlank()) {
            throw new IllegalArgumentException("Thiếu mã phiên đặt vé");
        }

        if (userId == null || userId <= 0) {
            throw new AccessDeniedException("Không xác định được người đặt vé");
        }

        TempBookingRequest tempBooking = redisService.getTempBooking(tempBookingId);

        if (tempBooking == null || tempBooking.getBookingRequest() == null) {
            throw new IllegalStateException("Phiên đặt vé không tồn tại hoặc đã hết hạn");
        }

        if (!userId.equals(tempBooking.getUserId())) {
            throw new AccessDeniedException("Bạn không có quyền thanh toán phiên đặt vé này");
        }

        BookingRequest request = tempBooking.getBookingRequest();
        validateRequest(request);

        if (request.getCreatedAt() == null) {
            throw new IllegalStateException("Phiên đặt vé không có thời gian tạo");
        }

        LocalDateTime expiresAt = request.getCreatedAt().plusMinutes(HOLD_MINUTES);

        if (!expiresAt.isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Phiên đặt vé đã hết hạn");
        }

        // Khóa suất chiếu bằng findByIdForUpdate(), cùng cơ chế với tạo, hủy và cleanup
        // booking.
        ShowtimeEntity showtime = getShowtime(request.getShowtimeId());

        // Kiểm tra lại vì có thể đã phải chờ khóa.
        if (!expiresAt.isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Phiên đặt vé đã hết hạn trong khi chờ xử lý");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));

        if (!redisService.isOwnsSeats(showtime.getId(), request.getSeatIds(), userId)) {
            throw new IllegalStateException("Bạn không còn giữ đủ các ghế của phiên đặt vé");
        }

        // Kiểm tra ghế đúng phòng, chưa thuộc booking PENDING/PAID và không bị giữ
        // trong database.
        List<SeatEntity> seats = validateSeats(request, showtime);

        BookingEntity booking = new BookingEntity();
        booking.setUser(user);
        booking.setShowtime(showtime);
        booking.setStatus(BookingStatus.PENDING);

        // Giữ mốc cũ để cleanup không tự cộng thêm 15 phút.
        booking.setCreatedAt(request.getCreatedAt());

        List<TicketEntity> tickets = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (SeatEntity seat : seats) {
            SeatStatusEntity seatStatus = seatStatusRepository.findBySeatIdAndShowtimeId(seat.getId(),
                    showtime.getId());

            if (seatStatus == null) {
                seatStatus = new SeatStatusEntity();
                seatStatus.setSeat(seat);
                seatStatus.setShowtime(showtime);
            }

            seatStatus.setStatus(SeatStatus.HELD);
            seatStatus.setHoldExpiresAt(expiresAt);
            seatStatusRepository.save(seatStatus);

            BigDecimal price = showtime.getBasePrice().add(seat.getSeatType().getSurcharge());

            TicketEntity ticket = new TicketEntity();
            ticket.setBooking(booking);
            ticket.setShowtime(showtime);
            ticket.setSeat(seat);
            ticket.setOriginalPrice(price);
            ticket.setDiscountAmount(BigDecimal.ZERO);
            ticket.setFinalPrice(price);

            tickets.add(ticket);
            total = total.add(price);
        }

        if (total.signum() <= 0) {
            throw new IllegalStateException("Tổng tiền booking không hợp lệ");
        }

        booking.setTickets(tickets);
        booking.setTotalPrice(total);

        // Kiểm tra sát thời điểm lưu.
        if (!expiresAt.isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Phiên đặt vé đã hết hạn trong khi xử lý");
        }

        return bookingRepository.saveAndFlush(booking).getId();
    }

    @Override
    @Transactional
    public void processPaymentResult(String transactionCode, boolean isSuccess) {
        PaymentEntity payment = paymentRepository.findByTransactionCode(transactionCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch: " + transactionCode));

        // Tránh xử lý lặp lại nếu giao dịch đã có kết quả (Idempotency)
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        BookingEntity booking = payment.getBooking();
        ShowtimeEntity showtime = showtimeRepository.findByIdForUpdate(booking.getShowtime().getId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy showtime"));
        entityManager.refresh(booking);
        entityManager.refresh(payment);

        // Kiểm tra nếu booking không còn ở trạng thái PENDING (ví dụ: đã bị hủy bởi
        // người dùng hoặc hệ thống)
        if (booking.getStatus() != BookingStatus.PENDING) {
            log.warn(
                    "Giao dịch {} cho booking {} không ở trạng thái PENDING (trạng thái hiện tại: {}). Không kích hoạt vé.",
                    transactionCode, booking.getId(), booking.getStatus());
            if (isSuccess) {
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setPaidAt(LocalDateTime.now());
                paymentRepository.save(payment);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
            }
            return;
        }

        List<Long> seatIds = booking.getTickets().stream()
                .map(t -> t.getSeat().getId())
                .toList();

        if (isSuccess) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            booking.setStatus(BookingStatus.PAID);

            // 3. Cập nhật trạng thái ghế trong database thành BOOKED
            for (TicketEntity ticket : booking.getTickets()) {
                SeatStatusEntity seatStatus = seatStatusRepository.findBySeatIdAndShowtimeId(
                        ticket.getSeat().getId(), showtime.getId());
                if (seatStatus == null) {
                    seatStatus = new SeatStatusEntity();
                    seatStatus.setSeat(ticket.getSeat());
                    seatStatus.setShowtime(showtime);
                }
                seatStatus.setStatus(SeatStatus.BOOKED);
                seatStatus.setHoldExpiresAt(null);
                seatStatusRepository.save(seatStatus);
            }

            // 4. Nhả key giữ ghế trên Redis sau khi commit thành công
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        redisService.releaseSeats(showtime.getId(), seatIds, booking.getUser().getId());
                    }
                });
            }
        } else {
            // Thanh toán thất bại: Chỉ đánh dấu Payment là FAILED
            // KHÔNG hủy booking, KHÔNG nhả ghế để người dùng có thể thanh toán lại trong
            // thời hạn giữ chỗ
            payment.setStatus(PaymentStatus.FAILED);
        }

        paymentRepository.save(payment);
        bookingRepository.saveAndFlush(booking);
    }

    @Override
    @Transactional
    public void cancelTempBooking(String tempBookingId, Long userId) {
        if (tempBookingId == null || tempBookingId.isBlank()) {
            throw new IllegalArgumentException("Mã phiên đặt vé không hợp lệ");
        }
        TempBookingRequest tempBooking = redisService.getTempBooking(tempBookingId);
        if (tempBooking == null) {
            return;
        }
        if (userId == null || !userId.equals(tempBooking.getUserId())) {
            throw new AccessDeniedException("Bạn không có quyền hủy phiên đặt vé này");
        }
        BookingRequest request = tempBooking.getBookingRequest();
        if (request != null && request.getShowtimeId() != null && request.getSeatIds() != null) {
            redisService.releaseSeats(request.getShowtimeId(), request.getSeatIds(), userId);
        }
        if (tempBooking.getBookingId() != null) {
            try {
                delete(tempBooking.getBookingId());
            } catch (Exception e) {
                // Booking trong DB có thể đã bị hủy hoặc không tồn tại
            }
        }
        redisService.deleteTempBooking(tempBookingId);
    }
}
