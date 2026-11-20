package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.model.request.BookingRequest;
import com.moviebooking.movie_booking.model.TempBookingRequest;
import com.moviebooking.movie_booking.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
@Service
public class RedisServiceImpl implements RedisService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean holdSeats(Long showtimeId, List<Long> seatIds, Long userId, int minutes) {
        return acquireSeats(showtimeId, seatIds, userId, minutes, false);
    }

    @Override
    public boolean reserveSeats(Long showtimeId, List<Long> seatIds, Long userId, int minutes) {
        return acquireSeats(showtimeId, seatIds, userId, minutes, true);
    }

    private boolean acquireSeats(Long showtimeId, List<Long> seatIds, Long userId,
                                 int minutes, boolean allowOwner) {
        // Check all keys before writing any key: a conflict never removes another hold.
        String script = """
                for _, key in ipairs(KEYS) do
                    local owner = redis.call('GET', key)
                    if owner and (ARGV[3] ~= 'true' or owner ~= ARGV[1]) then
                        return 0
                    end
                end
                for _, key in ipairs(KEYS) do
                    redis.call('SET', key, ARGV[1], 'EX', ARGV[2])
                end
                return 1
                """;
        Long result = stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                seatKeys(showtimeId, seatIds), userId.toString(),
                String.valueOf(Duration.ofMinutes(minutes).toSeconds()), String.valueOf(allowOwner));
        return Long.valueOf(1).equals(result);
    }

    private List<String> seatKeys(Long showtimeId, List<Long> seatIds) {
        return seatIds.stream()
                .map(seatId -> String.format("seat_hold:showtime:%d:seat:%d", showtimeId, seatId))
                .toList();
    }

    @Override
    public void releaseSeats(Long showtimeId, List<Long> seatIds, Long userId) {
        String script = """
                for _, key in ipairs(KEYS) do
                    if redis.call('GET', key) == ARGV[1] then
                        redis.call('DEL', key)
                    end
                end
                return 1
                """;
        stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                seatKeys(showtimeId, seatIds), userId.toString());
    }

    @Override
    public void releaseSeats(Long showtimeId, List<Long> seatIds){
        List<String> keys = seatIds.stream()
                .map(seatId ->
                        String.format("seat_hold:showtime:%d:seat:%d", showtimeId, seatId))
                .toList();
        stringRedisTemplate.delete(keys);
    }
    @Override
    public String saveTempBooking(BookingRequest bookingRequest, Long userId, int minutes){
        String bookingId = UUID.randomUUID().toString();
        try{
            TempBookingRequest tempBooking = new TempBookingRequest();
            tempBooking.setUserId(userId);
            tempBooking.setBookingRequest(bookingRequest);
            tempBooking.setTempBookingId(bookingId);
            String json = objectMapper.writeValueAsString(tempBooking);
            stringRedisTemplate.opsForValue().set("temp_booking:"+bookingId,json,Duration.ofMinutes(minutes));
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
        return bookingId;
    }
    @Override
    public TempBookingRequest getTempBooking(String bookingId){
        String json = stringRedisTemplate.opsForValue().get("temp_booking:"+bookingId);
        try {
            return json == null ? null : objectMapper.readValue(json, TempBookingRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("Không thể đọc phiên đặt vé tạm từ Redis", e);
        }
    }

    @Override
    public void updateTempBookingPaymentId(String bookingId, String paymentId) {
        String key = "temp_booking:" + bookingId;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            throw new IllegalStateException("Phiên đặt vé đã hết hạn hoặc không tồn tại");
        }

        Long ttlMillis = stringRedisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
        if (ttlMillis == null || ttlMillis <= 0) {
            throw new IllegalStateException("Phiên đặt vé đã hết hạn hoặc không tồn tại");
        }

        try {
            TempBookingRequest tempBooking = objectMapper.readValue(json, TempBookingRequest.class);
            tempBooking.setPaymentId(paymentId);
            String updatedJson = objectMapper.writeValueAsString(tempBooking);
            Boolean updated = stringRedisTemplate.opsForValue()
                    .setIfPresent(key, updatedJson, Duration.ofMillis(ttlMillis));
            if (!Boolean.TRUE.equals(updated)) {
                throw new IllegalStateException("Phiên đặt vé đã hết hạn hoặc không tồn tại");
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Không thể cập nhật mã thanh toán cho phiên đ  . . . . ặt vé", e);
        }
    }
    @Override
    public void updateTempBookingBookingId(String tempBookingId, Long savedBookingId) {
        String key = "temp_booking:" + tempBookingId;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            throw new IllegalStateException("Phiên đặt vé đã hết hạn hoặc không tồn tại");
        }

        Long ttlMillis = stringRedisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
        if (ttlMillis == null || ttlMillis <= 0) {
            throw new IllegalStateException("Phiên đặt vé đã hết hạn hoặc không tồn tại");
        }

        try {
            TempBookingRequest tempBooking = objectMapper.readValue(json, TempBookingRequest.class);
            tempBooking.setBookingId(savedBookingId);
            String updatedJson = objectMapper.writeValueAsString(tempBooking);
            Boolean updated = stringRedisTemplate.opsForValue()
                    .setIfPresent(key, updatedJson, Duration.ofMillis(ttlMillis));
            if (!Boolean.TRUE.equals(updated)) {
                throw new IllegalStateException("Phiên đặt vé đã hết hạn hoặc không tồn tại");
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Không thể cập nhật bookingId cho phiên đặt vé", e);
        }
    }

    @Override
    public void saveTxnTempMapping(String txnRef, String tempBookingId, int minutes) {
        if (txnRef == null || tempBookingId == null) return;
        stringRedisTemplate.opsForValue().set("txn_to_temp:" + txnRef, tempBookingId, Duration.ofMinutes(minutes));
    }

    @Override
    public String getTempBookingIdByTxnRef(String txnRef) {
        if (txnRef == null) return null;
        return stringRedisTemplate.opsForValue().get("txn_to_temp:" + txnRef);
    }

    @Override 
    public boolean isOwnsSeats(Long showtimeId, List<Long> seatIds, Long userId){
        if (showtimeId == null || userId == null || seatIds == null || seatIds.isEmpty()) {
            return false;
        }
        String script = """
                for _, key in ipairs(KEYS) do
                    local owner = redis.call('GET', key)
                    if owner ~= ARGV[1] then
                        return 0
                    end
                end
                return 1
                """;
        Long result = stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                seatKeys(showtimeId, seatIds), userId.toString());

        return Long.valueOf(1).equals(result);
    }
}
