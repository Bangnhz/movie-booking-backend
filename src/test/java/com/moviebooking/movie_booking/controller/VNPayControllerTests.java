package com.moviebooking.movie_booking.controller;

import com.moviebooking.movie_booking.entity.BookingEntity;
import com.moviebooking.movie_booking.entity.UserEntity;
import com.moviebooking.movie_booking.enums.BookingStatus;
import com.moviebooking.movie_booking.enums.PaymentStatus;
import com.moviebooking.movie_booking.model.TempBookingRequest;
import com.moviebooking.movie_booking.repository.BookingRepository;
import com.moviebooking.movie_booking.repository.PaymentRepository;
import com.moviebooking.movie_booking.repository.UserRepository;
import com.moviebooking.movie_booking.service.BookingService;
import com.moviebooking.movie_booking.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VNPayControllerTests {
    @Mock private BookingService bookingService;
    @Mock private RedisService redisService;
    @Mock private UserRepository userRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private HttpServletRequest request;
    @InjectMocks private VNPayController controller;

    @Test
    void createsPaymentFromOwnedTempBookingAndStoresVnpTransactionReference() throws Exception {
        UserEntity user = new UserEntity();
        user.setId(42L);
        when(userRepository.findByUsername("test-user")).thenReturn(Optional.of(user));

        TempBookingRequest tempBooking = new TempBookingRequest();
        tempBooking.setUserId(42L);
        when(redisService.getTempBooking("temp-booking-id")).thenReturn(tempBooking);

        when(bookingService.createPendingFromTemp("temp-booking-id", 42L)).thenReturn(100L);

        BookingEntity booking = new BookingEntity();
        booking.setId(100L);
        booking.setTotalPrice(new BigDecimal("100000"));
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        when(paymentRepository.findTopByBooking_IdAndStatusOrderByCreatedAtDesc(100L, PaymentStatus.PENDING))
                .thenReturn(Optional.empty());

        when(request.getHeader("X-FORWARDED-FOR")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        Map<String, String> response = controller.createPayment(
                "temp-booking-id",
                User.withUsername("test-user").password("password").authorities("USER").build(),
                request);

        String paymentUrl = response.get("paymentUrl");
        assertTrue(paymentUrl.contains("vnp_Amount=10000000"));
        String txnRef = getQueryParameter(paymentUrl, "vnp_TxnRef");
        assertEquals(8, txnRef.length());
        verify(redisService).saveTxnTempMapping(eq(txnRef), eq("temp-booking-id"), eq(15));
    }

    private String getQueryParameter(String url, String name) {
        String query = url.substring(url.indexOf('?') + 1);
        for (String parameter : query.split("&")) {
            String[] parts = parameter.split("=", 2);
            if (name.equals(parts[0])) {
                return URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            }
        }
        throw new AssertionError("Missing query parameter: " + name);
    }
}
