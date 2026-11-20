package com.moviebooking.movie_booking.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviebooking.movie_booking.config.VNPayConfig;
import com.moviebooking.movie_booking.entity.BookingEntity;
import com.moviebooking.movie_booking.model.dto.VNPayQueryResult;
import com.moviebooking.movie_booking.util.HmacSHA512;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class VNPayService {
    private static final Logger log = LoggerFactory.getLogger(VNPayService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final DateTimeFormatter VNP_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyyMMddHHmmss")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    public String createPaymentUrl(BookingEntity booking, String ipAddress) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = booking.getId().toString();
        String vnp_OrderInfo = "Thanh toan dat ve xem phim:" + booking.getId();
        String vnp_OrderType = "other";
        String vnp_Amount = booking.getTotalPrice().multiply(new BigDecimal(100)).toBigInteger().toString();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", VNPayConfig.vnp_TmnCode);
        vnp_Params.put("vnp_Amount", vnp_Amount);
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl);
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
            String vnp_SecureHash = HmacSHA512.hash(VNPayConfig.vnp_HashSecret, hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

            return VNPayConfig.vnp_PayUrl + "?" + queryUrl;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tra cứu trạng thái giao dịch với VNPay thông qua API QueryDR (2.1.0)
     */
    public VNPayQueryResult queryTransaction(String txnRef, LocalDateTime paymentCreatedAt) {
        try {
            String vnp_RequestId = VNPayConfig.getRandomNumber(8);
            String vnp_Version = "2.1.0";
            String vnp_Command = "querydr";
            String vnp_TmnCode = VNPayConfig.vnp_TmnCode;
            String vnp_OrderInfo = "Truy van giao dich " + txnRef;
            LocalDateTime transDate = (paymentCreatedAt != null) ? paymentCreatedAt : LocalDateTime.now();
            String vnp_TransactionDate = VNP_DATE_FORMATTER.format(transDate.atZone(ZoneId.systemDefault()));
            String vnp_CreateDate = VNP_DATE_FORMATTER.format(LocalDateTime.now().atZone(ZoneId.systemDefault()));
            String vnp_IpAddr = "127.0.0.1";

            String signData = vnp_RequestId + "|" + vnp_Version + "|" + vnp_Command + "|" + vnp_TmnCode + "|"
                    + txnRef + "|" + vnp_TransactionDate + "|" + vnp_CreateDate + "|" + vnp_IpAddr + "|" + vnp_OrderInfo;

            String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, signData);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("vnp_RequestId", vnp_RequestId);
            requestBody.put("vnp_Version", vnp_Version);
            requestBody.put("vnp_Command", vnp_Command);
            requestBody.put("vnp_TmnCode", vnp_TmnCode);
            requestBody.put("vnp_TxnRef", txnRef);
            requestBody.put("vnp_OrderInfo", vnp_OrderInfo);
            requestBody.put("vnp_TransactionDate", vnp_TransactionDate);
            requestBody.put("vnp_CreateDate", vnp_CreateDate);
            requestBody.put("vnp_IpAddr", vnp_IpAddr);
            requestBody.put("vnp_SecureHash", vnp_SecureHash);

            String jsonPayload = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(VNPayConfig.vnp_ApiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("VNPay QueryDR trả về mã HTTP: {} body: {}", response.statusCode(), response.body());
                return VNPayQueryResult.unknown("HTTP " + response.statusCode());
            }

            JsonNode jsonNode = objectMapper.readTree(response.body());
            String responseCode = jsonNode.path("vnp_ResponseCode").asText("");
            String transactionStatus = jsonNode.path("vnp_TransactionStatus").asText("");
            String message = jsonNode.path("vnp_Message").asText("");

            log.info("VNPay QueryDR kết quả cho txnRef {}: responseCode={}, transactionStatus={}, message={}",
                    txnRef, responseCode, transactionStatus, message);

            boolean isSuccess = "00".equals(responseCode) && "00".equals(transactionStatus);
            boolean isPending = "00".equals(responseCode) && "01".equals(transactionStatus);
            boolean isFailed = ("00".equals(responseCode) && "02".equals(transactionStatus)) || "91".equals(responseCode);
            boolean isUnknown = !isSuccess && !isPending && !isFailed;

            return VNPayQueryResult.builder()
                    .success(isSuccess)
                    .pending(isPending)
                    .failed(isFailed)
                    .unknown(isUnknown)
                    .responseCode(responseCode)
                    .transactionStatus(transactionStatus)
                    .message(message)
                    .build();

        } catch (Exception e) {
            log.error("Lỗi khi gọi VNPay QueryDR cho txnRef {}: {}", txnRef, e.getMessage(), e);
            return VNPayQueryResult.unknown("Exception: " + e.getMessage());
        }
    }
}
