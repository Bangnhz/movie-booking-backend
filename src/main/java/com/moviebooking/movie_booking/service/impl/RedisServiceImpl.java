package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.model.request.BookingRequest;
import com.moviebooking.movie_booking.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
@Service
public class RedisServiceImpl implements RedisService {
    private static final String SEAT_HOLD_PREFIX = "seat_hold:";
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean holdSeats(Long showtimeId, List<Long> seatIds, Long userId, int minutes) {
        List<String> heldKeys = new ArrayList<>();
        for(Long seatId : seatIds){
            String key = String.format(
                    "seat_hold:showtime:%d:seat:%d",
                    showtimeId,
                    seatId
            );
//            SET key value NX EX 300
            Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, userId.toString(), Duration.ofMinutes(minutes));

            if(!success){
                stringRedisTemplate.delete(key);
                return false;
            }
            heldKeys.add(key);
        }
        return true;
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
    public String saveTempBooking(BookingRequest bookingRequest, int minutes){
        String bookingId = UUID.randomUUID().toString();
        try{
            String json = objectMapper.writeValueAsString(bookingRequest);
            stringRedisTemplate.opsForValue().set("temp_booking:"+bookingId,json,Duration.ofMinutes(minutes));
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
        return bookingId;
    }
    @Override
    public BookingRequest getTempBooking(String bookingId){
        String json = stringRedisTemplate.opsForValue().get("temp_booking:"+bookingId);
        try {
            return json == null ? null : objectMapper.readValue(json, BookingRequest.class);
        } catch (Exception e) {
            return null;
        }
    }
}
