package com.moviebooking.movie_booking.service.impl;

import com.moviebooking.movie_booking.config.VNPayConfig;
import com.moviebooking.movie_booking.entity.BookingEntity;
import com.moviebooking.movie_booking.util.HmacSHA512;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {
    public String createPaymentUrl(BookingEntity booking, String ipAddress) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = booking.getId().toString(); // Dùng ID Booking làm mã giao dịch
        String vnp_OrderInfo = "Thanh toan dat ve xem phim:" + booking.getId();
        String vnp_OrderType = "other";
        String vnp_Amount = booking.getTotalPrice().multiply(new BigDecimal(100)).toBigInteger().toString(); // VNPay tính theo đơn vị xu (x100)

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", "MÃ_TMN_CỦA_ÔNG");
        vnp_Params.put("vnp_Amount", vnp_Amount);
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_ReturnUrl", "http://localhost:3000/payment-callback"); // FE nhận kết quả
        vnp_Params.put("vnp_IpAddr", ipAddress);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String createDate = formatter.format(new Date());

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        try {

            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);

            for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext();) {

                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);

                if (fieldValue != null && fieldValue.length() > 0) {

                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            String queryUrl = query.toString();

            String vnp_SecureHash =
                    HmacSHA512.hash(VNPayConfig.vnp_HashSecret, hashData.toString());

            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

            return VNPayConfig.vnp_PayUrl + "?" + queryUrl;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}