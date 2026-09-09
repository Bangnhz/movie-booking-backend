# 🎬 MovieBooking - Hệ Thống Đặt Vé Xem Phim Trực Tuyến (Online Movie Booking System)

> **Dự án Backend hoàn chỉnh phát triển trên nền tảng Java 23 & Spring Boot 3.3.5, áp dụng kiến trúc phân tầng (Layered Architecture), cơ chế Giữ ghế thời gian thực với Redis (Atomic Seat Locking), Thanh toán trực tuyến VNPay và Hệ thống Báo cáo - Thống kê Doanh thu đa chiều.**

---

## 📋 MỤC LỤC
1. [Giới thiệu & Ý nghĩa thực tế](#1-giới-thiệu--ý-nghĩa-thực-tế)
2. [Nhu cầu người dùng & Giải pháp đáp ứng](#2-nhu-cầu-người-dùng--giải-pháp-đáp-ứng)
3. [Danh sách các Module đã hoàn thành](#3-danh-sách-các-module-đã-hoàn-thành)
4. [Công nghệ & Kỹ thuật áp dụng](#4-công-nghệ--kỹ-thuật-áp-dụng)
5. [Điểm nổi bật về Kỹ thuật & Kiến trúc](#5-điểm-nổi-bật-về-kỹ-thuật--kiến-trúc)
6. [Cấu trúc Dự án (Project Structure)](#6-cấu-trúc-dự-án-project-structure)
7. [Hướng dẫn Cài đặt & Khởi chạy](#7-hướng-dẫn-cài-đặt--khởi-chạy)
8. [Tài liệu API Endpoint chính](#8-tài-liệu-api-endpoint-chính)

---

## 1. 🌟 GIỚI THIỆU & Ý NGHĨA THỰC TẾ

### Bối cảnh thực tế
Trong thời đại kỹ thuật số, nhu cầu giải trí và xem phim tại rạp (Cinema) tăng trưởng vượt bậc. Tuy nhiên, các hệ thống bán vé rạp chiếu phim truyền thống hoặc hệ thống cũ thường gặp phải các thách thức lớn:
- **Tình trạng trùng ghế (Double Booking / Race Condition)**: Nhiều người dùng cùng chọn và thanh toán một ghế ở cùng một thời điểm.
- **Tắc nghẽn hệ thống giờ cao điểm**: Lượng truy cập tăng đột biến khi mở bán vé các bộ phim bom tấn.
- **Hệ thống quản lý rời rạc**: Khó khăn trong việc quản lý danh mục phim, lịch chiếu theo từng phòng, bảng giá theo loại ghế và tổng hợp thống kê doanh thu theo thời gian thực.

### Ý nghĩa thực tế của Dự án
Dự án **MovieBooking** được thiết kế và triển khai nhằm giải quyết triệt để các bài toán thực tế trên bằng cách xây dựng một hệ thống đặt vé xem phim trực tuyến hiện đại, chịu tải tốt, đảm bảo tính chính xác tuyết đối về trạng thái ghế và mang lại trải nghiệm mượt mà cho khách hàng.

---

## 2. 🎯 NHU CẦU NGƯỜI DÙNG & GIẢI PHÁP ĐÁP ỨNG

### 👥 Đối với Khách Hàng (Customer / End-User)
* **Nhu cầu**: Tìm kiếm phim nhanh chóng, xem lịch chiếu theo rạp/tỉnh thành, chọn vị trí ghế yêu thích và thanh toán an toàn không phải xếp hàng tại rạp.
* **Giải pháp đáp ứng**:
  * **Tra cứu đa dạng**: Tìm kiếm phim theo tên, thể loại, rạp chiếu, độ tuổi (P, C13, C16, C18) và lịch chiếu theo ngày.
  * **Sơ đồ ghế trực quan**: Hiển thị trạng thái ghế thời gian thực (Ghế Trống, Ghế Đang Giữ, Ghế Đã Bán) cùng loại ghế (Thường, VIP, Sweetbox/Couple).
  * **Cơ chế Giữ ghế an toàn (Seat Holding)**: Tự động khóa ghế trong **15 phút** cho người dùng tiến hành thanh toán, đảm bảo không ai có thể cướp ghế trong lúc đang nhập thông tin thanh toán.
  * **Thanh toán linh hoạt & tiện lợi**: Tích hợp cổng **VNPay** hỗ trợ quét mã QR, thẻ ATM nội địa, Credit Card.
  * **Quản lý lịch sử đặt vé**: Tra cứu lại danh sách các vé đã đặt, thông tin phòng chiếu, thời gian chiếu và mã đơn hàng.

---

### 🛡️ Đối với Ban Quản Lý & Nhân Viên (Admin & Cinema Manager)
* **Nhu cầu**: Quản lý toàn bộ hệ thống rạp, phòng chiếu, phim, giá vé, lịch chiếu và theo dõi hiệu quả kinh doanh.
* **Giải pháp đáp ứng**:
  * **Quản lý Danh mục Rạp & Phòng chiếu**: Cấu hình số phòng, vị trí rạp theo Tỉnh/Thành phố, thiết lập sơ đồ ghế (hàng, cột, loại ghế và mức phụ thu tương ứng).
  * **Lập lịch chiếu linh hoạt (Showtime Scheduling)**: Xếp lịch chiếu phim cho từng phòng, ràng buộc không trùng giờ chiếu.
  * **Báo cáo & Thống kê Chuyên sâu (Dashboard Analytics)**: Theo dõi tổng doanh thu, số vé bán ra, tổng số suất chiếu, tỷ lệ lấp đầy ghế (Seat Occupancy Rate / Empty Rate) lọc theo tháng, năm, rạp hoặc phim.
  * **Phân quyền người dùng (Role-Based Access Control)**: Kiểm soát chặt chẽ quyền hạn giữa Quản trị viên (ADMIN), Nhân viên (STAFF) và Khách hàng (CUSTOMER).

---

## 3. 🧩 DANH SÁCH CÁC MODULE ĐÃ HOÀN THÀNH

System bao gồm **7 Module chính** được đóng gói chuẩn chỉnh:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        MOVIE BOOKING SYSTEM                            │
├───────────────┬────────────────┬──────────────────────┬────────────────┤
│ Authentication│ Movie & Genre  │ Cinema, City & Room  │ Showtime Mgmt  │
│  & Security   │   Management   │      Management      │                │
├───────────────┼────────────────┼──────────────────────┼────────────────┤
│ Real-time Seat│ VNPay Payment  │ Analytics Dashboard  │ User History & │
│ Lock & Booking│  Integration   │     & Reporting      │ Profile Mgmt   │
└───────────────┴────────────────┴──────────────────────┴────────────────┘
```

### 1️⃣ Module Xác Thực & Bảo Mật (Authentication & Security Module)
- Đăng ký tài khoản (`Register`), Đăng nhập (`Login`) mã hóa mật khẩu an toàn với **BCryptPasswordEncoder**.
- Xác thực Stateless dựa trên **JWT (JSON Web Token)** với `JwtFilter` và `JwtUtils`.
- Phân quyền người dùng chi tiết theo từng role (`ROLE_ADMIN`, `ROLE_CUSTOMER`, `ROLE_STAFF`).
- Tùy chỉnh `SecurityConfig` hỗ trợ CORS và bảo vệ các API riêng tư (`/bookings/**`, `/payments/**`, `/tickets/**`).

### 2️⃣ Module Quản Lý Phim & Thể Loại (Movie & Genre Management Module)
- **Quản lý Phim**: Tạo mới, cập nhật, xóa và tra cứu phim với các thuộc tính: Tiêu đề, Poster, Trích dẫn/Mô tả, Thời lượng, Ngày phát hành, Trạng thái (Đang chiếu / Sắp chiếu), Đánh giá độ tuổi (`AgeRating`: P, C13, C16, C18).
- **Quản lý Thể loại**: Quản lý danh mục thể loại phim và mối quan hệ đa - đa (`MovieGenreEntity`).
- **Tìm kiếm linh hoạt**: Lọc phim theo tiêu đề, thể loại, rạp chiếu hoặc trạng thái phim với phân trang (`Pageable`).

### 3️⃣ Module Quản Lý Tỉnh Thành, Rạp & Phòng Chiếu (Cinema & Location Module)
- **Tỉnh/Thành phố (`City`)**: Quản lý phân vùng địa lý (Hà Nội, TP.HCM, Đà Nẵng,...).
- **Rạp chiếu (`Cinema`)**: Thông tin rạp, địa chỉ, vị trí địa lý, quản lý danh sách phòng chiếu trực thuộc rạp.
- **Phòng chiếu (`Room`) & Sơ đồ ghế (`Seat`)**:
  - Khai báo hàng/cột ghế (A1, A2, B1, B2,...).
  - Phân loại loại ghế (`SeatType`): Ghế Thường, Ghế VIP, Ghế Couple với mức giá phụ thu (`surcharge`) khác nhau.

### 4️⃣ Module Quản Lý Lịch Chiếu (Showtime Management Module)
- Lập lịch chiếu phim theo Phòng chiếu + Phim + Thời gian bắt đầu/kết thúc + Giá vé cơ sở (`Base Price`).
- Kiểm tra tính hợp lệ của suất chiếu (chống xếp trùng lịch trong cùng một phòng chiếu).
- API tra cứu lịch chiếu theo phim, theo rạp và theo khoảng thời gian.

### 5️⃣ Module Giữ Ghế & Đặt Vé Thời Gian Thực (Real-Time Seat Locking & Booking)
- **Cơ chế Khóa ghế Redis (Distributed Seat Lock)**: Sử dụng lệnh Atomic `SET key value NX EX` giữ ghế tức thì trong 15 phút.
- **Ngăn chặn Double-Booking**: Khi một khách hàng đang giữ ghế, các khách hàng khác lập tức thấy trạng thái ghế bị khóa (`HELD`) và không thể chọn.
- **Lưu trữ đơn hàng tạm thời (`Temp Booking`)**: Lưu trữ thông tin đơn hàng dưới dạng JSON trong Redis với TTL 15 phút.
- **Tác vụ ngầm Tự động dọn dẹp (`Scheduled Cleanup Task`)**: Tự động quét các đơn hàng/ghế giữ quá hạn mỗi 60 giây (`@Scheduled(fixedRate = 60000)`) để giải phóng ghế về trạng thái `AVAILABLE` và chuyển đơn hàng sang `CANCELLED`.

### 6️⃣ Module Tích Hợp Thanh Toán VNPay (VNPay Payment Gateway)
- **Khởi tạo giao dịch thanh toán**: Sinh URL thanh toán chuẩn VNPay v2.1.0 kèm mã hóa chữ ký bảo mật **HMAC-SHA512**.
- **Xử lý IPN (Instant Payment Notification) & Callback Return**:
  - Kiểm tra tính hợp lệ của chữ ký phản hồi từ VNPay (Chống giả mạo dữ liệu / Anti-tampering).
  - Cập nhật trạng thái đơn hàng sang `PAID`, khởi tạo dữ liệu vé chính thức (`TicketEntity`).
  - Xóa dữ liệu tạm trong Redis và cập nhật trạng thái ghế vĩnh viễn sang `BOOKED`.

### 7️⃣ Module Thống Kê & Báo Cáo Doanh Thu (Analytics & Reporting Module)
- **Dashboard Thống kê tổng quan**:
  - Tổng doanh thu toàn hệ thống (`Total Revenue`).
  - Tổng số suất chiếu đã tổ chức (`Total Showtimes`).
  - Tổng số vé đã bán ra (`Total Tickets`).
  - Tổng số phim đang khai thác (`Total Movies`).
- **Thống kê nâng cao**:
  - Báo cáo doanh thu theo Phim, Rạp, Tháng, Năm.
  - Tỷ lệ ghế trống trung bình (`Average Empty Seat Rate`) phục vụ tối ưu hóa công suất phòng chiếu.
  - Sử dụng **Native SQL & Dynamic Queries** để tối ưu tốc độ xử lý các truy vấn thống kê lớn.

---

## 4. 🛠️ CÔNG NGHỆ & KỸ THUẬT ÁP DỤNG

| Hạng Mục | Công Nghệ / Kỹ Thuật | Mô Tả Chi Tiết |
| :--- | :--- | :--- |
| **Language & Platform** | **Java 23** | Sử dụng phiên bản Java LTS mới nhất với hiệu năng vượt trội và cú pháp hiện đại. |
| **Framework Core** | **Spring Boot 3.3.5** | Framework phổ biến hàng đầu cho phát triển ứng dụng Java Enterprise Web RESTful APIs. |
| **Security** | **Spring Security 6 + JJWT 0.11.5** | Cơ chế xác thực JWT Stateless, mã hóa mật khẩu an toàn với BCrypt. |
| **Database & ORM** | **MySQL 8.3 + Spring Data JPA** | Hệ quản trị CSDL quan hệ kết hợp ORM Hibernate, hỗ trợ Custom Repository & Dynamic Native SQL. |
| **In-Memory & Locking** | **Redis + Spring Data Redis** | Cấu trúc dữ liệu In-Memory Key-Value, TTL (Time-To-Live), `SETNX` triển khai Distributed Lock giữ ghế. |
| **Payment Integration** | **VNPay API v2.1.0** | Tích hợp cổng thanh toán trực tuyến bảo mật bằng mã hóa chữ ký **HMAC-SHA512**. |
| **Background Processing**| **Spring Task Scheduler (`@Scheduled`)** | Tác vụ chạy ngầm định kỳ quét dọn đơn hàng và giải phóng ghế quá hạn giữ. |
| **Data Mapping** | **ModelMapper 3.2.0 + Custom Converters** | Chuyển đổi linh hoạt giữa Entity, DTO, Request/Response Payload. |
| **Development Tools** | **Lombok 1.18.30 + Gradle** | Giảm thiểu mã lặp (Boilerplate code) và quản lý dependency tự động. |

---

## 5. 💡 ĐIỂM NỔI BẬT VỀ KỸ THUẬT & KIẾN TRÚC

### 1. Kiến Trúc Phân Tầng Chuẩn Enterprise (Layered Architecture)
Dự án tuân thủ nghiêm ngặt mô hình **Controller - Service - ServiceImpl - Repository - Entity - DTO**:
- **Controller**: Tiếp nhận REST requests, validate dữ liệu đầu vào.
- **Service & ServiceImpl**: Chứa toàn bộ Business Logic nghiệp vụ.
- **Repository / CustomerImpl**: Quản lý truy vấn dữ liệu (JPA & Native SQL).
- **Entity**: Khai báo ánh xạ CSDL quan hệ.
- **DTO / Request / Response**: Đảm bảo an toàn thông tin, không làm rò rỉ cấu trúc CSDL ra bên ngoài.

### 2. Xử Lý Trùng Ghế Thời Gian Thực Vượt Trội (Atomic Redis Locking)
Thay vì khóa dòng dưới CSDL (Database Row Locking / Pessimistic Lock) gây giảm hiệu năng hệ thống khi hàng nghìn người cùng truy cập, dự án triển khai **Distributed Lock trên Redis**:
```java
// Giữ ghế tức thì với Atomic Operation SETNX & TTL 15 phút
Boolean success = stringRedisTemplate.opsForValue()
    .setIfAbsent("seat_hold:showtime:" + showtimeId + ":seat:" + seatId, userId.toString(), Duration.ofMinutes(15));
```
Giải pháp này phản hồi cực nhanh (Latency < 5ms) và tự động hết hạn mà không làm treo CSDL.

### 3. Tác Vụ Ngầm Tự Động Dọn Dẹp (Automated Background Scheduler)
Tích hợp `@Scheduled(fixedRate = 60000)` tự động quét các giao dịch PENDING quá hạn:
```java
@Scheduled(fixedRate = 60000)
@Transactional
public void cleanupExpiredBookings() {
    LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(15);
    List<BookingEntity> expiredBookings = bookingRepository.findAllByStatusAndCreatedAtBefore(BookingStatus.PENDING, expiredTime);
    // Auto release seats and cancel bookings...
}
```

### 4. Tích Hợp Thanh Toán Bảo Mật Với Checksum HMAC-SHA512
Triển khai thuật toán tính toán chữ ký số an toàn chống lại các cuộc tấn công thay đổi tham số giao dịch (Parameter Tampering):
```java
String vnp_SecureHash = HmacSHA512.hash(VNPayConfig.vnp_HashSecret, hashData.toString());
```

---

## 6. 📂 CẤU TRÚC DỰ ÁN (PROJECT STRUCTURE)

```
movie_booking
├── build.gradle
├── settings.gradle
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── moviebooking
    │   │           └── movie_booking
    │   │               ├── MovieBookingApplication.java
    │   │               ├── config/                # Cấu hình Security, CORS, Redis, VNPay, ModelMapper
    │   │               │   └── security/          # JwtFilter, JwtUtils
    │   │               ├── controller/            # REST API Controllers (Auth, Movie, Booking, Payment, VNPay,...)
    │   │               ├── converter/             # Converter chuyển đổi Entity <-> DTO
    │   │               ├── entity/                # JPA Entities & Embedded IDs
    │   │               ├── enums/                 # AgeRating, BookingStatus, SeatStatus, PaymentStatus,...
    │   │               ├── model/                 # DTOs, Request & Response objects
    │   │               ├── repository/            # JPA Repositories & Custom Native Repositories
    │   │               │   └── customer/
    │   │               │       └── impl/          # Native Dynamic SQL implementations
    │   │               ├── service/               # Interfaces nghiệp vụ
    │   │               │   └── impl/              # Business Logic Implementations & Background Tasks
    │   │               └── util/                  # Utility classes (HmacSHA512,...)
    │   └── resources
    │       ├── application.properties             # Cấu hình CSDL MySQL, Redis, JWT, VNPay Keys
    │       ├── static/
    │       └── templates/
    └── test/
```

---

## 7. 🚀 HƯỚNG DẪN CÀI ĐẶT & KHỞI CHẠY

### ⚙️ Yêu cầu môi trường (Prerequisites)
- **Java Development Kit (JDK)**: Version 23 hoặc 17+
- **Database**: MySQL 8.0+
- **Cache**: Redis Server 6.0+
- **Build Tool**: Gradle 8.x (đi kèm wrapper `gradlew`)

### 1. Clone dự án & Cấu hình CSDL
1. Clone dự án về máy cục bộ:
   ```bash
   git clone https://github.com/your-username/movie_booking.git
   cd movie_booking
   ```
2. Tạo CSDL MySQL:
   ```sql
   CREATE DATABASE movie_booking_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. Khởi động Redis Server:
   ```bash
   redis-server
   ```

### 2. Cấu hình file `application.properties`
Mở file `src/main/resources/application.properties` và điều chỉnh các thông số kết nối:
```properties
# Spring App Config
spring.application.name=movie_booking
server.port=8080

# MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/movie_booking_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379

# JWT Config
jwt.secret=YourSuperSecretKeyForJWTTokenGenerationMovieBooking2026Secure
jwt.expiration=86400000

# VNPay Sandbox Config
vnpay.tmnCode=YOUR_TMN_CODE
vnpay.hashSecret=YOUR_HASH_SECRET
vnpay.payUrl=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.returnUrl=http://localhost:8080/api/payment/vnpay/return
```

### 3. Biên dịch & Khởi chạy Ứng dụng
Chạy lệnh Gradle để build và khởi chạy server:
```bash
# Windows
.\gradlew.bat bootRun

# Linux / MacOS
./gradlew bootRun
```
Khi ứng dụng khởi chạy thành công, REST APIs sẽ sẵn sàng tại `http://localhost:8080`.

---

## 8. 📡 TÀI LIỆU API ENDPOINT CHÍNH

### 🔑 Auth & User Management (`/api/auth`, `/api/users`)
| HTTP Method | Endpoint | Quyền hạn | Mô tả |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Public | Đăng ký tài khoản người dùng mới |
| `POST` | `/api/auth/login` | Public | Đăng nhập & nhận JWT Token |
| `GET` | `/api/users/profile` | Authenticated | Lấy thông tin cá nhân |
| `GET` | `/api/users` | Admin | Tra cứu danh sách người dùng (có phân trang) |

### 🎬 Movies & Showtimes (`/api/movies`, `/api/showtimes`)
| HTTP Method | Endpoint | Quyền hạn | Mô tả |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/movies` | Public | Danh sách phim (Tìm kiếm, Lọc, Phân trang) |
| `GET` | `/api/movies/{id}` | Public | Xem chi tiết thông tin phim |
| `POST` | `/api/movies` | Admin | Thêm mới phim |
| `GET` | `/api/showtimes` | Public | Tra cứu lịch chiếu theo rạp / phim |

### 🎟️ Seat Holding & Booking (`/api/bookings`)
| HTTP Method | Endpoint | Quyền hạn | Mô tả |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/bookings/hold` | Authenticated | Giữ ghế thời gian thực 15 phút qua Redis |
| `GET` | `/api/bookings/temp/{uuid}` | Authenticated | Lấy thông tin đơn hàng tạm từ Redis |
| `GET` | `/api/bookings/my-bookings` | Authenticated | Tra cứu lịch sử đặt vé của người dùng |

### 💳 Payment & VNPay Integration (`/api/payment/vnpay`)
| HTTP Method | Endpoint | Quyền hạn | Mô tả |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/payment/vnpay/create` | Authenticated | Khởi tạo URL thanh toán VNPay |
| `GET` | `/api/payment/vnpay/return` | Public | Fe nhận kết quả chuyển hướng từ VNPay |
| `GET` | `/api/payment/vnpay/ipn` | Public | Callback IPN xử lý kết quả giao dịch từ VNPay |

### 📊 Analytics & Reporting (`/api/statistics`)
| HTTP Method | Endpoint | Quyền hạn | Mô tả |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/statistics/dashboard` | Admin | Lấy tổng quan doanh thu, vé bán, suất chiếu |
| `POST` | `/api/statistics` | Admin | Báo cáo doanh thu & tỷ lệ lấp đầy ghế chi tiết |

---

## 🤝 LỜI KẾT & PHÁT TRIỂN TƯƠNG LAI
Hệ thống **MovieBooking** được hoàn thiện với độ chỉn chu cao về cả mặt kiến trúc lập trình lẫn nghiệp vụ thực tế. Trong các phiên bản tiếp theo, dự án có thể mở rộng thêm các tính năng:
- [ ] Gửi vé xem phim qua Email / SMS kèm mã **QR Code** để quét vé tại cửa rạp.
- [ ] Tích hợp Microservices & Kafka cho hệ thống thông báo thời gian thực.
- [ ] Tích hợp thêm các ví điện tử khác (MoMo, ZaloPay, ShopeePay).

---
*Dự án được xây dựng và duy trì bởi đội ngũ phát triển Java Backend.*
