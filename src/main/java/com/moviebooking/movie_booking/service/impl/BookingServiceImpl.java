package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.converter.MovieConverter;
import com.moviebooking.movie_booking.converter.SeatConverter;
import com.moviebooking.movie_booking.entity.*;
import com.moviebooking.movie_booking.enums.BookingStatus;
import com.moviebooking.movie_booking.enums.SeatStatus;
import com.moviebooking.movie_booking.model.dto.MovieBookingDTO;
import com.moviebooking.movie_booking.model.dto.ShowtimeSeatDTO;
import com.moviebooking.movie_booking.model.request.BookingRequest;
import com.moviebooking.movie_booking.model.request.search.BookingSearchRequest;
import com.moviebooking.movie_booking.model.response.BookingResponse;
import com.moviebooking.movie_booking.model.response.BookingSearchResponse;
import com.moviebooking.movie_booking.repository.*;
import com.moviebooking.movie_booking.service.BookingService;
import com.moviebooking.movie_booking.service.RedisService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BookingServiceImpl implements BookingService {
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
    private MovieRepository movieRepository;
    @Autowired
    private SeatConverter seatConverter;
    @Autowired
    private MovieConverter movieConverter;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private RedisService redisService;

    @Override
    @Transactional
    public Long create(BookingRequest bookingRequest) {
//        List<BookingEntity> oldPendingBookings = bookingRepository.findAllByUserIdAndShowtimeIdAndStatus(bookingRequest.getUserId(),bookingRequest.getShowtimeId(), BookingStatus.PENDING);

//        for(BookingEntity oldBooking : oldPendingBookings){
//            for(TicketEntity oldTicket : oldBooking.getTickets()){
//                SeatStatusEntity seatStatus = seatStatusRepository.findBySeatIdAndShowtimeId(oldTicket.getSeat().getId(), bookingRequest.getShowtimeId());
//                seatStatus.setStatus(SeatStatus.AVAILABLE);
//                seatStatus.setHoldExpiresAt(null);
//                seatStatusRepository.save(seatStatus);
//            }
//            oldBooking.setStatus(BookingStatus.CANCELLED);
//            bookingRepository.save(oldBooking);
//        }
//        bookingRepository.flush();

        BookingEntity bookingEntity = new BookingEntity();
        bookingEntity.setCreatedAt(LocalDateTime.now());
        bookingEntity.setStatus(BookingStatus.PENDING);

        ShowtimeEntity showtimeEntity = showtimeRepository.findById(bookingRequest.getShowtimeId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy showtime"));
        bookingEntity.setShowtime(showtimeEntity);

        UserEntity userEntity = userRepository.findById(bookingRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        bookingEntity.setUser(userEntity);

        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal basePrice = showtimeEntity.getBasePrice();

        List<TicketEntity> tickets = new ArrayList<>();
        for(Long seatId : bookingRequest.getSeatIds()){
            SeatEntity seatEntity = seatRepository.findById(seatId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy ghế"));
            if(ticketRepository.existsByShowtimeIdAndSeatIdAndBooking_StatusIn(showtimeEntity.getId(), seatId, new BookingStatus[]{BookingStatus.PENDING})){
                throw new RuntimeException("Ghế "+seatEntity.getRowIndex()+""+seatEntity.getColumnIndex()+" đã có người đặt");
            }
            SeatStatusEntity seatStatus = seatStatusRepository.findBySeatIdAndShowtimeId(seatId, showtimeEntity.getId());
            if(seatStatus == null){
                seatStatus = new SeatStatusEntity();
                seatStatus.setSeat(seatEntity);
                seatStatus.setShowtime(showtimeEntity);
                seatStatus.setStatus(SeatStatus.AVAILABLE);
            }
            if(seatStatus.getStatus() != SeatStatus.AVAILABLE){
                throw new RuntimeException("Ghế "+seatEntity.getRowIndex()+""+seatEntity.getColumnIndex()+" đang được đặt");
            }
            seatStatus.setStatus(SeatStatus.HELD);
            seatStatus.setHoldExpiresAt(LocalDateTime.now().plusMinutes(5));
//            seatStatusRepository.save(seatStatus);
            BigDecimal seatPrice =
                    seatEntity.getSeatType().getSurcharge()
                            .add(basePrice);
            totalPrice = totalPrice.add(seatPrice);

//            TicketEntity ticketEntity = new TicketEntity();
//            ticketEntity.setSeat(seatEntity);
//            ticketEntity.setShowtime(showtimeEntity);
//            ticketEntity.setBooking(bookingEntity);
//            ticketEntity.setOriginalPrice(seatPrice);
//            ticketEntity.setFinalPrice(seatPrice);
//            tickets.add(ticketEntity);
        }
//        bookingEntity.setTotalPrice(totalPrice);
//        bookingEntity.setTickets(tickets);
//        bookingRepository.save(bookingEntity);
//        ticketRepository.saveAll(tickets);
        return bookingEntity.getId();
    }

    @Override
    public void delete(Long id) {
        UserEntity userEntity = userRepository.findById(id).orElseThrow(()->new RuntimeException("Không tìm thấy user id="+id));
        userRepository.deleteById(userEntity.getId());
    }

    @Override
    public String holdBooking(BookingRequest bookingRequest) {
        for(Long seatId : bookingRequest.getSeatIds()){
            SeatEntity seatEntity = seatRepository.findById(seatId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy ghế id="+seatId));
            if(ticketRepository.existsByShowtimeIdAndSeatIdAndBooking_StatusIn(bookingRequest.getShowtimeId(), seatId, new BookingStatus[]{BookingStatus.PAID})){
                throw new RuntimeException("Ghế "+seatEntity.getRowIndex()+""+seatEntity.getColumnIndex()+" đã có người đặt");
            }
        }
        bookingRequest.setCreatedAt(LocalDateTime.now());
        boolean holdSuccess = redisService.holdSeats(bookingRequest.getShowtimeId(), bookingRequest.getSeatIds(), bookingRequest.getUserId(),15);
        if(!holdSuccess){
            throw new RuntimeException("Ghế đang được người khác thanh toán!");
        }
        String tmpBookingId = redisService.saveTempBooking(bookingRequest,15);
        return tmpBookingId;
    }

//    @Override
//    public BookingResponse getBookingResponse(Long bookingId) {
//        BookingResponse bookingResponseDTO = new BookingResponse();
//        BookingEntity bookingEntity = bookingRepository.findById(bookingId)
//                .orElseThrow(() -> new RuntimeException());
//                bookingResponseDTO.setId(bookingEntity.getId());
//        bookingResponseDTO.setStatus(bookingEntity.getStatus());
//        bookingResponseDTO.setTotalPrice(bookingEntity.getTotalPrice());
//        bookingResponseDTO.setCreatedAt(bookingEntity.getCreatedAt());
//        bookingResponseDTO.setExpirationTime(bookingEntity.getCreatedAt().plusMinutes(5));
//        ShowtimeEntity showtimeEntity = bookingEntity.getShowtime();
//
//        MovieBookingDTO movieBookingDTO = movieConverter.toMovieBookingDTO(showtimeEntity.getMovie());
//        bookingResponseDTO.setMovieBookingDTO(movieBookingDTO);
//
//        bookingResponseDTO.setCinemaName(showtimeEntity.getRoom().getCinema().getName());
//        bookingResponseDTO.setRoomName(showtimeEntity.getRoom().getName());
//        bookingResponseDTO.setStartTime(showtimeEntity.getStartTime());
//        List<SeatEntity> seatEntities = bookingEntity.getTickets().stream().map(ticket -> ticket.getSeat()).toList();
//        List<ShowtimeSeatDTO> seatDTOS = seatEntities.stream().map(seat -> seatConverter.toSeatDTO(seat,showtimeEntity.getId())).toList();
//        bookingResponseDTO.setSelectedSeats(seatDTOS);
//
//        return bookingResponseDTO;
//    }

    @Override
    public BookingResponse getTempBookingResponse(String uuid) {
        BookingRequest bookingRequest = redisService.getTempBooking(uuid);
        if(bookingRequest ==  null){
            throw new RuntimeException("Phiên đặt vé đã hết hạn! Vui lòng đặt lại");
        }
        BookingResponse bookingResponse = new BookingResponse();
        bookingResponse.setId(uuid);
        bookingResponse.setCreatedAt(LocalDateTime.now().plusMinutes(5));
        ShowtimeEntity showtimeEntity = showtimeRepository.findById(bookingRequest.getShowtimeId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy showtime"));
        bookingResponse.setCinemaName(showtimeEntity.getRoom().getCinema().getName());
        bookingResponse.setRoomName(showtimeEntity.getRoom().getName());
        bookingResponse.setStartTime(showtimeEntity.getStartTime());
        List<SeatEntity> seatEntities = seatRepository.findAllById(bookingRequest.getSeatIds());
        List<ShowtimeSeatDTO> showtimeSeatDTOS = seatEntities.stream().map(seat -> seatConverter.toSeatDTO(seat,showtimeEntity.getId())).toList();
        bookingResponse.setSelectedSeats(showtimeSeatDTOS);

        bookingResponse.setStatus(BookingStatus.PENDING);
        BigDecimal basePrice = showtimeEntity.getBasePrice();
        BigDecimal totalPrice = seatEntities.stream().map(seatEntity -> seatEntity.getSeatType().getSurcharge()
                .add(basePrice)).reduce(BigDecimal.ZERO, BigDecimal::add);
        bookingResponse.setTotalPrice(totalPrice);
        bookingResponse.setCreatedAt(bookingRequest.getCreatedAt());
        bookingResponse.setExpirationTime(bookingRequest.getCreatedAt().plusMinutes(15));

        MovieBookingDTO movieBookingDTO = movieConverter.toMovieBookingDTO(showtimeEntity.getMovie());
        bookingResponse.setMovieBookingDTO(movieBookingDTO);

        return bookingResponse;
    }

    @Override
    public Page<BookingSearchResponse> getBookingsByUserId(Long userId, BookingSearchRequest request, Pageable pageable) {
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

}
