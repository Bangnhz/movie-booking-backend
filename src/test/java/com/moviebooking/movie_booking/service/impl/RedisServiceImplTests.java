package com.moviebooking.movie_booking.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviebooking.movie_booking.model.TempBookingRequest;
import com.moviebooking.movie_booking.model.request.BookingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisServiceImplTests {
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    @InjectMocks private RedisServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "objectMapper", objectMapper);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void savesTempBookingIdInsideRedisValue() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setShowtimeId(2L);

        String bookingId = service.saveTempBooking(request, 42L, 15);

        org.mockito.ArgumentCaptor<String> json = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(eq("temp_booking:" + bookingId), json.capture(), eq(Duration.ofMinutes(15)));
        TempBookingRequest saved = objectMapper.readValue(json.getValue(), TempBookingRequest.class);
        assertEquals(bookingId, saved.getTempBookingId());
        assertEquals(42L, saved.getUserId());
        assertEquals(2L, saved.getBookingRequest().getShowtimeId());
    }

    @Test
    void updatesPaymentIdWithoutExtendingRedisExpiration() throws Exception {
        String bookingId = "temp-booking-id";
        String key = "temp_booking:" + bookingId;
        TempBookingRequest tempBooking = new TempBookingRequest();
        tempBooking.setTempBookingId(bookingId);
        when(valueOperations.get(key)).thenReturn(objectMapper.writeValueAsString(tempBooking));
        when(redisTemplate.getExpire(key, TimeUnit.MILLISECONDS)).thenReturn(30_000L);
        when(valueOperations.setIfPresent(eq(key), anyString(), eq(Duration.ofMillis(30_000))))
                .thenReturn(true);

        service.updateTempBookingPaymentId(bookingId, "vnp-txn-ref");

        org.mockito.ArgumentCaptor<String> json = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(valueOperations).setIfPresent(eq(key), json.capture(), eq(Duration.ofMillis(30_000)));
        TempBookingRequest updated = objectMapper.readValue(json.getValue(), TempBookingRequest.class);
        assertEquals(bookingId, updated.getTempBookingId());
        assertEquals("vnp-txn-ref", updated.getPaymentId());
    }
}
