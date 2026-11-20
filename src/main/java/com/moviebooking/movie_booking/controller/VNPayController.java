package com.moviebooking.movie_booking.controller;

import com.moviebooking.movie_booking.config.VNPayConfig;
import com.moviebooking.movie_booking.entity.BookingEntity;
import com.moviebooking.movie_booking.entity.PaymentEntity;
import com.moviebooking.movie_booking.enums.BookingStatus;
import com.moviebooking.movie_booking.enums.PaymentStatus;
import com.moviebooking.movie_booking.model.TempBookingRequest;
import com.moviebooking.movie_booking.repository.BookingRepository;
import com.moviebooking.movie_booking.repository.PaymentRepository;
import com.moviebooking.movie_booking.repository.UserRepository;
import com.moviebooking.movie_booking.service.BookingService;
import com.moviebooking.movie_booking.service.RedisService;
import com.moviebooking.movie_booking.util.HmacSHA512;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Controller
@RequestMapping ("/api/payment/vnpay")
public class VNPayController {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private BookingRepository bookingRepository;

    @PostMapping("/create")
    @ResponseBody
    public Map<String, String> createPayment(
            @RequestParam String tempBookingId,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest req) throws Exception {
        if (userDetails == null) {
            throw new AccessDeniedException("Vui lòng đăng nhập để thanh toán");
        }
        Long userId = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"))
                .getId();

        // Kiểm tra xem tempBooking đã có BookingEntity trong DB chưa (trường hợp thanh toán lại)
        TempBookingRequest tempBooking = redisService.getTempBooking(tempBookingId);
        if (tempBooking == null) {
            throw new IllegalStateException("Phiên đặt vé đã hết hạn hoặc không tồn tại");
        }
        if (!userId.equals(tempBooking.getUserId())) {
            throw new AccessDeniedException("Bạn không có quyền thanh toán phiên đặt vé này");
        }

        BookingEntity bookingEntity;
        if (tempBooking.getBookingId() != null) {
            // Thanh toán lại cho booking PENDING đã tạo trước đó
            bookingEntity = bookingRepository.findById(tempBooking.getBookingId())
                    .orElseThrow(() -> new IllegalStateException("Không tìm thấy booking"));
            if (bookingEntity.getStatus() != BookingStatus.PENDING) {
                throw new IllegalStateException("Đơn đặt vé không ở trạng thái chờ thanh toán");
            }
        } else {
            // Lần đầu bấm thanh toán: Chuyển dữ liệu từ Redis sang DB
            Long bookingId = bookingService.createPendingFromTemp(tempBookingId, userId);
            bookingEntity = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new IllegalStateException("Không tìm thấy booking vừa tạo"));
            // Lưu lại bookingId lên Redis để dùng lại nếu bị lỗi thanh toán
            redisService.updateTempBookingBookingId(tempBookingId, bookingId);
        }

        long amount = bookingEntity.getTotalPrice().longValueExact();

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_OrderInfo = "Thanh toan ve phim";
        String orderType = "other";
        String vnp_IpAddr = VNPayConfig.getIpAddress(req);
        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;

        // 1. Kiểm tra xem booking đã có giao dịch nào đang ở trạng thái PENDING chưa (tránh request lặp/double-click)
        Optional<PaymentEntity> pendingPaymentOpt = paymentRepository
                .findTopByBooking_IdAndStatusOrderByCreatedAtDesc(bookingEntity.getId(), PaymentStatus.PENDING);

        String vnp_TxnRef;
        LocalDateTime paymentCreatedAt;
        if (pendingPaymentOpt.isPresent()) {
            // Request bị gửi lặp: Tái sử dụng giao dịch đang chờ, không tạo mã mới và không ghi đè
            PaymentEntity existingPayment = pendingPaymentOpt.get();
            vnp_TxnRef = existingPayment.getTransactionCode();
            paymentCreatedAt = existingPayment.getCreatedAt();
        } else {
            // Lần đầu hoặc lần trước đã xác nhận FAILED: Tạo PaymentEntity MỚI với txnRef riêng
            vnp_TxnRef = VNPayConfig.getRandomNumber(8);
            paymentCreatedAt = LocalDateTime.now();
            PaymentEntity payment = new PaymentEntity();
            payment.setBooking(bookingEntity);
            payment.setAmount(bookingEntity.getTotalPrice());
            payment.setMethod("VNPAY");
            payment.setStatus(PaymentStatus.PENDING);
            payment.setTransactionCode(vnp_TxnRef);
            payment.setCreatedAt(paymentCreatedAt);
            paymentRepository.save(payment);

            // Lưu mapping txnRef -> tempBookingId lên Redis để hỗ trợ lấy lại phiên khi thất bại
            redisService.saveTxnTempMapping(vnp_TxnRef, tempBookingId, 15);
        }

        Map vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(Math.multiplyExact(amount, 100)));
        vnp_Params.put("vnp_CurrCode", "VND");
        String bank_code = req.getParameter("bankcode");
        if (bank_code != null && !bank_code.isEmpty()) {
            vnp_Params.put("vnp_BankCode", bank_code);
        }
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", orderType);

        String locate = req.getParameter("language");
        if (locate != null && !locate.isEmpty()) {
            vnp_Params.put("vnp_Locale", locate);
        } else {
            vnp_Params.put("vnp_Locale", "vn");
        }
        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        DateTimeFormatter vnpDateFormatter = DateTimeFormatter
                .ofPattern("yyyyMMddHHmmss")
                .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
        String vnp_CreateDate = vnpDateFormatter.format(
                (paymentCreatedAt != null ? paymentCreatedAt : LocalDateTime.now()).atZone(ZoneId.systemDefault()));

        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        // Tính thời gian hết hạn theo thời gian giữ ghế ban đầu
        Instant now = Instant.now();
        Instant expiresAt = bookingEntity.getCreatedAt()
            .plusMinutes(15)
            .atZone(ZoneId.systemDefault())
            .toInstant();
        if (!expiresAt.isAfter(now)) {
            throw new IllegalStateException(
                    "Phiên đặt vé đã hết hạn, vui lòng chọn ghế lại"
            );
        }
        vnp_Params.put("vnp_ExpireDate", vnpDateFormatter.format(expiresAt));

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                //Build query
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
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + queryUrl;
        return Map.of("paymentUrl", paymentUrl);
    }

    @GetMapping("/return")
    public void paymentReturn(HttpServletRequest request,
                              HttpServletResponse response) throws IOException {

        // /return chỉ chuyển hướng người dùng về trang kết quả ở Frontend.
        // Tuyệt đối KHÔNG cập nhật DB tại đây để tránh giả mạo và lỗi không đồng bộ.
        // Việc xác thực và ghi nhận kết quả thanh toán do IPN webhook đảm nhiệm.
        String txnRef = request.getParameter("vnp_TxnRef");
        String redirectUrl = "http://localhost:3000/payment-result?txnRef=" + (txnRef != null ? txnRef : "");
        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/status")
    @ResponseBody
    public ResponseEntity<?> getPaymentStatus(
            @RequestParam String txnRef,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập");
        }

        PaymentEntity payment = paymentRepository.findByTransactionCode(txnRef)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch: " + txnRef));

        BookingEntity booking = payment.getBooking();
        // Kiểm tra quyền sở hữu (chủ đơn)
        if (!booking.getUser().getUsername().equals(userDetails.getUsername())) {
            throw new AccessDeniedException("Bạn không có quyền xem thông tin giao dịch này");
        }

        String tempBookingId = redisService.getTempBookingIdByTxnRef(txnRef);
        boolean isExpired = false;
        if (tempBookingId != null) {
            TempBookingRequest tempBooking = redisService.getTempBooking(tempBookingId);
            if (tempBooking == null) {
                isExpired = true;
            } else if (tempBooking.getBookingRequest() != null && tempBooking.getBookingRequest().getCreatedAt() != null) {
                isExpired = !tempBooking.getBookingRequest().getCreatedAt().plusMinutes(15).isAfter(LocalDateTime.now());
            }
        } else {
            isExpired = booking.getCreatedAt() == null || !booking.getCreatedAt().plusMinutes(15).isAfter(LocalDateTime.now());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("txnRef", txnRef);
        response.put("status", payment.getStatus().name());
        response.put("amount", payment.getAmount());
        response.put("tempBookingId", tempBookingId);
        response.put("isExpired", isExpired);
        response.put("bookingId", booking.getId());
        response.put("createdAt", payment.getCreatedAt());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/ipn")
    @ResponseBody
    public String ipn(HttpServletRequest request) throws Exception {

        Map<String, String> fields = new HashMap<>();

        Enumeration<String> params = request.getParameterNames();

        while (params.hasMoreElements()) {

            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);

            if (fieldValue != null && fieldValue.length() > 0) {
                fields.put(fieldName, fieldValue);
            }
        }

        String secureHash = fields.remove("vnp_SecureHash");

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();

        for (String name : fieldNames) {

            hashData.append(name)
                    .append("=")
                    .append(URLEncoder.encode(fields.get(name), "US-ASCII"))
                    .append("&");

        }

        hashData.deleteCharAt(hashData.length() - 1);

        String checkHash =
                HmacSHA512.hash(VNPayConfig.vnp_HashSecret, hashData.toString());

        if (!checkHash.equals(secureHash)) {
            return "{\"RspCode\":\"97\",\"Message\":\"Invalid Checksum\"}";
        }

        String responseCode = request.getParameter("vnp_ResponseCode");
        String txnRef = request.getParameter("vnp_TxnRef");
        String amountParam = request.getParameter("vnp_Amount");

        Optional<PaymentEntity> paymentOpt = paymentRepository.findByTransactionCode(txnRef);
        if (paymentOpt.isEmpty()) {
            return "{\"RspCode\":\"01\",\"Message\":\"Order not found\"}";
        }

        PaymentEntity payment = paymentOpt.get();

        // Kiểm tra số tiền
        long vnpAmount = Long.parseLong(amountParam) / 100;
        if (payment.getAmount().longValueExact() != vnpAmount) {
            return "{\"RspCode\":\"04\",\"Message\":\"Invalid Amount\"}";
        }

        // Kiểm tra nếu đơn hàng đã được xử lý rồi (Idempotency)
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return "{\"RspCode\":\"02\",\"Message\":\"Order already confirmed\"}";
        }

        // Cập nhật kết quả thanh toán
        boolean isSuccess = "00".equals(responseCode);
        bookingService.processPaymentResult(txnRef, isSuccess);

        return "{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}";
    }
}
