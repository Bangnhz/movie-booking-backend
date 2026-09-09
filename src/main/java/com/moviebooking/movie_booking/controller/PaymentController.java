package com.moviebooking.movie_booking.controller;

import com.moviebooking.movie_booking.config.VNPayConfig;
import com.moviebooking.movie_booking.util.HmacSHA512;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;


@RestController
@RequestMapping ("/api/payment")
public class PaymentController {
    @PostMapping("/vnpay-ipn")
    public Map<String,String> createPayment(
            @RequestParam long amount,
            HttpServletRequest request) throws Exception {

        String vnp_TxnRef = UUID.randomUUID().toString();
        String vnp_IpAddr = request.getRemoteAddr();

        Map<String,String> params = new HashMap<>();

        params.put("vnp_Version","2.1.0");
        params.put("vnp_Command","pay");
        params.put("vnp_TmnCode", VNPayConfig.vnp_TmnCode);
        params.put("vnp_Amount", String.valueOf(amount*100));
        params.put("vnp_CurrCode","VND");
        params.put("vnp_TxnRef", vnp_TxnRef);
        params.put("vnp_OrderInfo","Thanh toan ve phim");
        params.put("vnp_OrderType","other");
        params.put("vnp_Locale","vn");
        params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl);
        params.put("vnp_IpAddr", vnp_IpAddr);

        SimpleDateFormat formatter =
                new SimpleDateFormat("yyyyMMddHHmmss");

        String createDate = formatter.format(new Date());

        params.put("vnp_CreateDate",createDate);

        List<String> fieldNames =
                new ArrayList<>(params.keySet());

        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String name : fieldNames){

            String value = params.get(name);

            hashData.append(name)
                    .append("=")
                    .append(value)
                    .append("&");

            query.append(name)
                    .append("=")
                    .append(URLEncoder.encode(value,"UTF-8"))
                    .append("&");
        }

        hashData.deleteCharAt(hashData.length()-1);

        String secureHash =
                HmacSHA512.hash(VNPayConfig.vnp_HashSecret, hashData.toString());

        query.append("vnp_SecureHash=")
                .append(secureHash);

        String paymentUrl =
                VNPayConfig.vnp_PayUrl + "?" + query;

        return Map.of("paymentUrl",paymentUrl);
    }
}
