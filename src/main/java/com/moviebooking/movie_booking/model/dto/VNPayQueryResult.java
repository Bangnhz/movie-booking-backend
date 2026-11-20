package com.moviebooking.movie_booking.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VNPayQueryResult {
    private boolean success;
    private boolean failed;
    private boolean pending;
    private boolean unknown;
    private String responseCode;
    private String transactionStatus;
    private String message;

    public static VNPayQueryResult unknown(String message) {
        return VNPayQueryResult.builder()
                .unknown(true)
                .message(message)
                .build();
    }
}
