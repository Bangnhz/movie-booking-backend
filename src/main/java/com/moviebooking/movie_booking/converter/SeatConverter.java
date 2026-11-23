package com.moviebooking.movie_booking.converter;

import com.moviebooking.movie_booking.entity.SeatEntity;
import com.moviebooking.movie_booking.entity.SeatStatusEntity;
import com.moviebooking.movie_booking.entity.SeatTypeEntity;
import com.moviebooking.movie_booking.enums.SeatStatus;
import com.moviebooking.movie_booking.model.dto.RoomSeatDTO;
import com.moviebooking.movie_booking.model.dto.ShowtimeSeatDTO;
import com.moviebooking.movie_booking.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

@Component
public class SeatConverter {

    @Autowired(required = false)
    private RedisService redisService;

    public SeatConverter() {
    }

    public SeatConverter(RedisService redisService) {
        this.redisService = redisService;
    }

    public void setRedisService(RedisService redisService) {
        this.redisService = redisService;
    }

    public ShowtimeSeatDTO toSeatDTO(SeatEntity seatEntity, Long showtimeId) {
        return toSeatDTO(seatEntity, showtimeId, null);
    }

    public ShowtimeSeatDTO toSeatDTO(SeatEntity seatEntity, Long showtimeId, Set<Long> heldSeatIdsInRedis) {
        ShowtimeSeatDTO showtimeSeatDTO = new ShowtimeSeatDTO();
        showtimeSeatDTO.setId(seatEntity.getId());
        showtimeSeatDTO.setColumnIndex(seatEntity.getColumnIndex());
        showtimeSeatDTO.setRowIndex(seatEntity.getRowIndex());

        SeatTypeEntity seatType = seatEntity.getSeatType();
        if (seatType != null) {
            showtimeSeatDTO.setType(seatType.getName());
            showtimeSeatDTO.setSurcharge(seatType.getSurcharge());
        }

        SeatStatusEntity currentStatus = null;
        if (seatEntity.getStatuses() != null) {
            currentStatus = seatEntity.getStatuses().stream()
                    .filter(ss -> ss.getShowtime() != null && ss.getShowtime().getId().equals(showtimeId))
                    .findFirst().orElse(null);
        }

        if (currentStatus != null) {
            if (currentStatus.getStatus() == SeatStatus.BOOKED) {
                showtimeSeatDTO.setStatus(SeatStatus.BOOKED);
                return showtimeSeatDTO;
            }
            if (currentStatus.getStatus() == SeatStatus.HELD) {
                // Chỉ coi là HELD nếu holdExpiresAt chưa quá hạn
                if (currentStatus.getHoldExpiresAt() == null || currentStatus.getHoldExpiresAt().isAfter(LocalDateTime.now())) {
                    showtimeSeatDTO.setStatus(SeatStatus.HELD);
                    return showtimeSeatDTO;
                }
            }
        }

        // Kiểm tra ghế có đang được giữ trên Redis không
        boolean isHeldInRedis;
        if (heldSeatIdsInRedis != null) {
            isHeldInRedis = heldSeatIdsInRedis.contains(seatEntity.getId());
        } else if (redisService != null && showtimeId != null && seatEntity.getId() != null) {
            isHeldInRedis = redisService.isSeatHeld(showtimeId, seatEntity.getId());
        } else {
            isHeldInRedis = false;
        }

        if (isHeldInRedis) {
            showtimeSeatDTO.setStatus(SeatStatus.HELD);
        } else {
            showtimeSeatDTO.setStatus(SeatStatus.AVAILABLE);
        }

        return showtimeSeatDTO;
    }

    public RoomSeatDTO toRoomSeatDTO(SeatEntity seatEntity) {
        RoomSeatDTO seatDTO = new RoomSeatDTO();
        seatDTO.setId(seatEntity.getId());
        seatDTO.setColumnIndex(seatEntity.getColumnIndex());
        seatDTO.setRowIndex(seatEntity.getRowIndex());

        SeatTypeEntity seatType = seatEntity.getSeatType();
        if (seatType != null) {
            seatDTO.setType(seatType.getName());
            seatDTO.setSurcharge(seatType.getSurcharge());
        }
        return seatDTO;
    }
}
