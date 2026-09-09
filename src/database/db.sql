DROP DATABASE IF EXISTS movie_booking_db;
CREATE DATABASE movie_booking_db;
USE movie_booking_db;

-- MOVIES
CREATE TABLE movies (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        title VARCHAR(255) NOT NULL,
                        age_rating ENUM('P', 'K', 'T13', 'T16', 'T18') DEFAULT 'P',
                        description TEXT,
                        duration INT NOT NULL,
                        release_date DATE,
                        poster_url VARCHAR(500),
                        is_active BOOLEAN DEFAULT TRUE
);

-- GENRES
CREATE TABLE genres (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(100) NOT NULL
);

-- MOVIE_GENRES (N-N)
CREATE TABLE movie_genres (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              movie_id INT NOT NULL,
                              genre_id INT NOT NULL,
                              FOREIGN KEY (movie_id) REFERENCES movies(id),
                              FOREIGN KEY (genre_id) REFERENCES genres(id),
                              UNIQUE (movie_id, genre_id)
);
CREATE TABLE cities (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(100) NOT NULL
);
-- CINEMAS
CREATE TABLE cinemas (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         address VARCHAR(255),
                         email VARCHAR(255) NOT NULL,
                         city_id INT NOT NULL,
                         FOREIGN KEY (city_id) REFERENCES cities(id)
);

-- ROOMS
CREATE TABLE rooms (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       cinema_id INT NOT NULL,
                       FOREIGN KEY (cinema_id) REFERENCES cinemas(id)
);

-- SEAT TYPES
CREATE TABLE seat_types (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(50) NOT NULL,
                            surcharge DECIMAL(10,2) DEFAULT 0
);

-- SEATS
CREATE TABLE seats (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       room_id INT NOT NULL,
                       row_index CHAR(2) NOT NULL,
                       column_index INT NOT NULL,
                       seat_type_id INT NOT NULL,
                       FOREIGN KEY (room_id) REFERENCES rooms(id),
                       FOREIGN KEY (seat_type_id) REFERENCES seat_types(id),
                       UNIQUE (room_id, row_index, column_index)
);

-- SHOWTIMES
CREATE TABLE showtimes (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           movie_id INT NOT NULL,
                           room_id INT NOT NULL,
                           start_time DATETIME NOT NULL,
                           end_time DATETIME,
                           base_price DECIMAL(10,2) NOT NULL,
                           FOREIGN KEY (movie_id) REFERENCES movies(id),
                           FOREIGN KEY (room_id) REFERENCES rooms(id)
);

-- SEAT STATUS (QUAN TRỌNG)
CREATE TABLE seat_status (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             seat_id INT NOT NULL,
                             showtime_id INT NOT NULL,
                             status ENUM('AVAILABLE','HELD','BOOKED') DEFAULT 'AVAILABLE',
                             hold_expires_at DATETIME ,
                             FOREIGN KEY (seat_id) REFERENCES seats(id),
                             FOREIGN KEY (showtime_id) REFERENCES showtimes(id),
                             UNIQUE (seat_id, showtime_id),
                             INDEX idx_hold_expiry (hold_expires_at)
);

-- 1. Bảng Roles: Lưu danh sách các quyền
CREATE TABLE roles (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE -- Ví dụ: 'ROLE_ADMIN', 'ROLE_USER'
);

-- 2. Bảng Users: Lưu thông tin người dùng
CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       fullname VARCHAR(255) NOT NULL,
                       username VARCHAR(255) NOT NULL UNIQUE,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       phone VARCHAR(20) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       points INT DEFAULT 0
);

-- 3. Bảng Trung gian User_Role: Kết nối User và Role
CREATE TABLE user_role (
                           user_id INT NOT NULL,
                           role_id INT NOT NULL,
                           PRIMARY KEY (user_id, role_id),
                           CONSTRAINT FK_User FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                           CONSTRAINT FK_Role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);
-- BOOKINGS
CREATE TABLE bookings (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          user_id INT NOT NULL,
                          showtime_id INT NOT NULL,
                          total_price DECIMAL(10,2) NOT NULL,
                          status ENUM('PENDING','PAID','CANCELLED') DEFAULT 'pending',
                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (user_id) REFERENCES users(id),
                          FOREIGN KEY (showtime_id) REFERENCES showtimes(id)
);

-- TICKETS
CREATE TABLE tickets (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         booking_id INT NOT NULL,
                         showtime_id INT NOT NULL,
                         seat_id INT NOT NULL,
                         original_price DECIMAL(10,2) DEFAULT 0.00,
                         discount_amount DECIMAL(10,2) DEFAULT 0.00,
                         final_price DECIMAL(10,2) NOT NULL,
                         FOREIGN KEY (booking_id) REFERENCES bookings(id),
                         FOREIGN KEY (seat_id) REFERENCES seats(id),
                         FOREIGN KEY (showtime_id) REFERENCES showtimes(id)
--                          UNIQUE (seat_id, showtime_id)

);
-- CREATE UNIQUE INDEX idx_active_seats
--     ON tickets(seat_id, showtime_id)
--     WHERE (status != 'CANCELLED');
CREATE TABLE payments (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          booking_id INT NOT NULL,
                          amount DECIMAL(10,2) NOT NULL,
                          method VARCHAR(50), -- momo, vnpay, card
                          status ENUM('PENDING','SUCCESS','FAILED','REFUNDED') DEFAULT 'PENDING',
                          transaction_code VARCHAR(255),
                          paid_at DATETIME,
                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

                          FOREIGN KEY (booking_id) REFERENCES bookings(id)
);
-- PRICING RULES
CREATE TABLE pricing_rules (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               rule_type VARCHAR(50)  NOT NULL, -- WEEKEND, HOLIDAY
                               multiplier DECIMAL(5,2)  NOT NULL DEFAULT 1.00,
                               start_time DATETIME NOT NULL,
                               end_time DATETIME,
                               priority INT DEFAULT 0,
                               is_active BOOLEAN DEFAULT TRUE
);

-- CREATE TABLE coupons(
-- 	id,code,discount_type,discount_value,min_order_value,
--     max_discount,max_used,used_count,description,
--     apply_to,start_date,end_date
-- )
-- CREATE TABLE coupon_rules(
-- 	id,coupon_id,rule_type,operator,value
-- )

--  Comment
CREATE TABLE movie_reviews (
                               id INT PRIMARY KEY,
                               rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5), -- Cột quan trọng nhất
                               comment_text VARCHAR(256), -- Nội dung nhận xét (có thể để trống)
                               created_at DATETIME DEFAULT NOW(),
                               user_id INT NOT NULL,
                               movie_id INT NOT NULL,
                               CONSTRAINT FK_Review_User FOREIGN KEY (user_id) REFERENCES users(id),
                               CONSTRAINT FK_Review_Movie FOREIGN KEY (movie_id) REFERENCES movies(id)
);
INSERT INTO genres (name) VALUES ('Hành động'), ('Tình cảm'), ('Hài hước'), ('Gia đình');
INSERT INTO movies (title, description, duration, release_date, poster_url, age_rating)
VALUES
    ('Thỏ ơi', 'Phim hoạt hình gia đình vui nhộn', 100, '2026-02-14', 'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/thumbnail/190x260/2e2b8cd282892c71872b9e67d2cb5039/t/o/to_poster_official_tiectet_3x4_fa.jpg','T18'),
    ('Nhà ba tôi', 'Phim tâm lý tình cảm gia đình Việt Nam', 120, '2026-02-20', 'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/thumbnail/190x260/2e2b8cd282892c71872b9e67d2cb5039/n/h/nh_ba_t_i_m_t_ph_ng_poster_-_kc_m_ng_1_t_t_2026.jpg','P'),
    ('KHỦNG LONG ĐÓN TẾT', 'Thể loại: Gia đình, Hoạt Hình, Phiêu Lưu', 91, '2026-02-26','https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/thumbnail/190x260/2e2b8cd282892c71872b9e67d2cb5039/n/h/nh_m_nh_i_th_i_poster_cgv.jpg', 'P'),
    ('CẢM ƠN NGƯỜI ĐÃ THỨC CÙNG TÔI', 'Thể loại: Gia đình, Tình cảm', 137, '2026-02-27', 'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/thumbnail/190x260/2e2b8cd282892c71872b9e67d2cb5039/m/a/main_condtct_cinema_low.jpg', 'K'),
    ('CHUYỆN KINH DỊ GIỚI SIÊU GIÀU', 'Thể loại: Kinh Dị', 95, '2026-02-27', 'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/thumbnail/190x260/2e2b8cd282892c71872b9e67d2cb5039/3/5/350x495-janur.jpg', 'T18'),
    ('TÀI', 'Thể loại: Gia đình, Hành Động, Tâm Lý', 101, '2026-03-06', 'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/thumbnail/190x260/2e2b8cd282892c71872b9e67d2cb5039/t/a/ta_i_-_poster_teaser_1_w70xh100_.jpg', 'T16'),
    ('The dominATE Experience TOUR DIỄN TOÀN CẦU', 'Thể loại: Hòa nhạc, Phim tài liệu', 146, '2026-03-06', 'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/thumbnail/190x260/2e2b8cd282892c71872b9e67d2cb5039/4/7/470x700-straykids.jpg', 'P'),
    ('QUỶ NHẬP TRÀNG 2', 'Thể loại: Hồi hộp, Kinh Dị', 126, '2026-03-06', 'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/thumbnail/190x260/2e2b8cd282892c71872b9e67d2cb5039/1/3/1316wx1920h-qnt.jpg', 'T18'),
    ('TỘI PHẠM 101', 'Thể loại: Hồi hộp, Tội phạm', 110, '2026-03-13', 'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/thumbnail/190x260/2e2b8cd282892c71872b9e67d2cb5039/p/o/poster-crime.jpg', 'T16'),
    ('CÚ NHẢY KỲ DIỆU', 'Thể loại: Gia đình, Hài, Hoạt Hình, Phiêu Lưu', 95, '2026-03-13', 'https://iguov8nhvyobj.vcdn.cloud/media/catalog/product/cache/1/thumbnail/190x260/2e2b8cd282892c71872b9e67d2cb5039/p/o/poster_cu_nhay_ky_dieu_.jpg', 'P');
INSERT INTO cities (id, name) VALUES
                                  (1, 'Hà Nội'),
                                  (2, 'TP. Hồ Chí Minh'),
                                  (3, 'Quảng Ninh');

INSERT INTO cinemas (name, address, email, city_id) VALUES
-- Hà Nội (ID 1)
('Lotte Cinema Hà Đông', 'Tầng 4, Mê Linh Plaza, Tô Hiệu', 'hadong@lotte.vn', 1),
('CGV Vincom Nguyễn Chí Thanh', '54A Nguyễn Chí Thanh, Láng Thượng', 'nct@cgv.vn', 1),
('CGV Aeon Hà Đông', 'Tầng 3, TTTM Aeon Mall Hà Đông', 'hadong.aeon@cgv.vn', 1),

-- TP. Hồ Chí Minh (ID 2)
('CGV Giga Mall', 'Tầng 6, Giga Mall, Thủ Đức', 'gigamall@cgv.vn', 2),
('Lotte Cinema Nam Sài Gòn', 'Tầng 3, Lotte Mart Quận 7', 'q7@lotte.vn', 2),

-- Quảng Ninh (ID 3)
('CGV Vincom Hạ Long', 'Lầu 4, Vincom Center, Bạch Đằng', 'halong@cgv.vn', 3),
('CGV Marine Plaza', 'Khu đô thị Hùng Thắng, Bãi Cháy', 'marine@cgv.vn', 3);
INSERT INTO rooms (name, cinema_id) VALUES
-- Hà Nội
('Phòng 1 - Lotte Hà Đông', 1), ('Phòng 2 - Lotte Hà Đông', 1),
('Phòng Gold Class - NCT', 2), ('Phòng 2 - NCT', 2),
('Phòng 1 - Aeon Hà Đông', 3), ('Phòng 2 - Aeon Hà Đông', 3),

-- TP. Hồ Chí Minh
('Phòng IMAX - Giga Mall', 4), ('Phòng 2 - Giga Mall', 4),
('Phòng 1 - Nam Sài Gòn', 5), ('Phòng 2 - Nam Sài Gòn', 5),

-- Quảng Ninh
('Phòng 1 - Vincom Hạ Long', 6), ('Phòng 2 - Vincom Hạ Long', 6),
('Phòng 1 - Marine Plaza', 7), ('Phòng 2 - Marine Plaza', 7);

INSERT INTO seat_types (name, surcharge)
VALUES ('Standard', 0.00), ('VIP', 15000.00), ('Sweetbox', 40000.00);

-- 1. FIX PROCEDURE TẠO GHẾ (Thêm DELIMITER đúng chuẩn)
DROP PROCEDURE IF EXISTS GenerateSeatsForAllRooms;
DELIMITER //

CREATE PROCEDURE GenerateSeatsForAllRooms()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE current_room_id INT;
    DECLARE row_char CHAR(1);
    DECLARE col_val INT;
    DECLARE seat_type INT;

    DECLARE room_cursor CURSOR FOR SELECT id FROM rooms;
DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

OPEN room_cursor;

read_loop: LOOP
        FETCH room_cursor INTO current_room_id;
        IF done THEN LEAVE read_loop; END IF;

        SET row_char = 'A';
        WHILE row_char <= 'E' DO
            SET col_val = 1;
            WHILE col_val <= 10 DO
                IF row_char IN ('A', 'B', 'C') THEN SET seat_type = 1;
ELSE SET seat_type = 2;
END IF;

INSERT INTO seats (room_id, row_index, column_index, seat_type_id)
VALUES (current_room_id, row_char, col_val, seat_type);

SET col_val = col_val + 1;
END WHILE;
            SET row_char = CHAR(ASCII(row_char) + 1);
END WHILE;
END LOOP;

CLOSE room_cursor;
END //
DELIMITER ;

-- GỌI TẠO 1200 GHẾ
CALL GenerateSeatsForAllRooms();

-- 2. INSERT SHOWTIMES (Đổ dữ liệu cho nhiều rạp và phim khác nhau)
-- Quy tắc: Giá cơ bản từ 70k - 120k tùy phim và khung giờ
INSERT INTO showtimes (movie_id, room_id, start_time, end_time, base_price)
VALUES

-- PHIM 1: Thỏ ơi (Chiếu tại Hà Nội - Lotte Hà Đông)
(1, 1, '2026-05-02 08:30:00', '2026-05-02 10:10:00', 70000),
(1, 2, '2026-05-02 10:00:00', '2026-05-02 11:40:00', 80000),
(1, 3, '2026-05-02 15:00:00', '2026-05-02 16:40:00', 90000),

-- PHIM 2: Nhà ba tôi (Chiếu tại Hà Nội - CGV Nguyễn Chí Thanh)
(2, 4, '2026-05-03 18:00:00', '2026-05-03 20:00:00', 100000),
(2, 5, '2026-05-03 20:30:00', '2026-05-03 22:30:00', 110000),

-- PHIM 3: KHỦNG LONG ĐÓN TẾT (Chiếu tại TP.HCM - CGV Giga Mall)
(3, 7, '2026-05-04 09:15:00', '2026-05-04 10:46:00', 85000),
(3, 8, '2026-05-04 13:00:00', '2026-05-04 14:31:00', 95000),

-- PHIM 5: CHUYỆN KINH DỊ GIỚI SIÊU GIÀU (Chiếu suất đêm tại TP.HCM - Lotte Nam Sài Gòn)
(5, 9, '2026-05-05 22:00:00', '2026-05-05 23:35:00', 90000),
(5, 10, '2026-05-05 23:30:00', '2026-05-06 01:05:00', 95000),

-- PHIM 8: QUỶ NHẬP TRÀNG 2 (Chiếu tại Quảng Ninh - CGV Hạ Long)
(8, 11, '2026-05-06 19:30:00', '2026-05-06 21:36:00', 90000),
(8, 12, '2026-05-06 21:00:00', '2026-05-06 23:06:00', 90000),

-- PHIM 6: TÀI (Chiếu suất sáng tại Quảng Ninh - CGV Marine)
(6, 13, '2026-05-07 10:00:00', '2026-05-07 11:41:00', 75000),
(6, 14, '2026-05-07 14:00:00', '2026-05-07 15:41:00', 85000);

-- 3. CẬP NHẬT TRẠNG THÁI GHẾ CHO CÁC SUẤT CHIẾU (Mẫu cho suất chiếu ID 1)
-- Trong thực tế, bước này nên làm tự động bằng Trigger khi insert Showtime hoặc xử lý ở Backend.
-- Ở đây tôi chèn mẫu cho suất chiếu đầu tiên để ông có cái mà test.
INSERT INTO seat_status (seat_id, showtime_id, status)
SELECT id, 1, 'available' FROM seats WHERE room_id = 1;

-- 4. PRICING RULES (Giữ nguyên của ông)
INSERT INTO pricing_rules (rule_type, multiplier, start_time, end_time, priority, is_active)
VALUES
    ('MORNING_DEAL', 0.80, '2026-01-01 08:00:00', '2026-12-31 11:00:00', 2, TRUE),
    ('LATE_NIGHT', 1.10, '2026-01-01 22:00:00', '2026-12-31 23:59:59', 1, TRUE);

INSERT INTO pricing_rules (rule_type, multiplier, start_time, end_time, priority, is_active)
VALUES
    ('MORNING_DEAL', 0.80, '2026-01-01 08:00:00', '2026-12-31 11:00:00', 2, TRUE), -- Giảm 20% sáng sớm
    ('LATE_NIGHT', 1.10, '2026-01-01 22:00:00', '2026-12-31 23:59:59', 1, TRUE);   -- Tăng 10% đêm khuya
INSERT INTO roles (name) VALUES
                             ('ADMIN'),
                             ('USER');
INSERT INTO users (fullname, username, email, phone, password, points) VALUES
                                                                           ('Nguyễn Văn Nam', 'nam', 'nam.nguyen@example.com', '0905123456', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41FS6', 120),
                                                                           ('Trần Thị Hương', 'huong', 'huong.tran@example.com', '0916234567', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41FS6', 80),
                                                                           ('Phạm Minh Tuấn', 'tuan', 'tuan.pham@example.com', '0927345678', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41FS6', 200),
                                                                           ('Lê Thảo My', 'mysoi', 'my.le@example.com', '0938456789', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41FS6', 60),
                                                                           ('Đỗ Quang Huy', 'huyD5', 'huy.do@example.com', '0949567890', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41FS6', 0),
                                                                           ('Người dùng B', 'b', 'b@example.com', '0123456789', '$2a$10$f3lVpYpX2.A7f6E2GfH.GeD7I5B3W8K5f9m6R5p4v.7q8r9s0t1u2', 200),
                                                                           ('Quản trị viên', 'admin', 'admin@moviebooking.com', '0999888777', '$2a$10$f3lVpYpX2.A7f6E2GfH.GeD7I5B3W8K5f9m6R5p4v.7q8r9s0t1u2', 0);
-- Giả sử ID của ROLE_ADMIN là 1, ROLE_USER là 2
-- ID của users tự tăng từ 1 đến 5

INSERT INTO user_role (user_id, role_id) VALUES
                                             (1, 1), -- ADMIN
                                             (2, 2), -- USER
                                             (3, 2), -- USER
                                             (4, 2), -- USER
                                             (5, 2), -- USER
                                             (6, 2),
                                             (7, 1);
-- =========================================================================
-- 1. BƠM TRẠNG THÁI GHẾ TRỐNG (AVAILABLE) CHO TẤT CẢ CÁC SUẤT CHIẾU CÒN LẠI
-- =========================================================================
-- (Để đảm bảo khóa ngoại bảng seat_status không bị lỗi khi insert ticket)
INSERT IGNORE INTO seat_status (seat_id, showtime_id, status)
SELECT s.id, st.id, 'AVAILABLE'
FROM seats s
         JOIN showtimes st ON s.room_id = st.room_id;


-- =========================================================================
-- 2. INSERT SỐ LƯỢNG LỚN BOOKINGS (Mô phỏng doanh thu thực tế)
-- =========================================================================
-- Xóa dữ liệu cũ của 3 bảng này trước để tránh trùng lặp nếu ông đã chạy script trước
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE payments;
TRUNCATE TABLE tickets;
TRUNCATE TABLE bookings;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO bookings (id, user_id, showtime_id, total_price, status, created_at)
VALUES
-- Suất 1 (Phim 1, Phòng 1): Giá base 70k
(1, 2, 1, 140000.00, 'PAID', '2026-05-01 14:30:00'),
(2, 3, 1, 170000.00, 'PAID', '2026-05-01 15:00:00'), -- Có ghế VIP
(3, 4, 1, 70000.00, 'CANCELLED', '2026-05-01 09:00:00'),
(4, 5, 1, 140000.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 5 MINUTE)),

-- Suất 2 (Phim 1, Phòng 2): Giá base 80k
(5, 6, 2, 160000.00, 'PAID', '2026-05-02 09:00:00'),
(6, 2, 2, 190000.00, 'PAID', '2026-05-02 09:15:00'),

-- Suất 3 (Phim 1, Phòng 3): Giá base 90k
(7, 3, 3, 180000.00, 'PAID', '2026-05-02 14:00:00'),

-- Suất 4 (Phim 2, Phòng 4): Giá base 100k (CGV Nguyễn Chí Thanh)
(8, 4, 4, 200000.00, 'PAID', '2026-05-03 17:00:00'),
(9, 5, 4, 230000.00, 'PAID', '2026-05-03 17:15:00'),

-- Suất 6 (Phim 3, Phòng 7): Giá base 85k (Giga Mall)
(10, 6, 6, 170000.00, 'PAID', '2026-05-04 08:30:00'),
(11, 2, 6, 200000.00, 'CANCELLED', '2026-05-04 08:45:00'),

-- Suất 8 (Phim 5, Phòng 9): Giá base 90k (Suất khuya)
(12, 3, 8, 180000.00, 'PAID', '2026-05-05 21:00:00'),
(13, 4, 8, 210000.00, 'PAID', '2026-05-05 21:15:00'),

-- Suất 10 (Phim 8, Phòng 11): Giá base 90k (Quảng Ninh)
(14, 5, 10, 180000.00, 'PAID', '2026-05-06 18:30:00'),
(15, 6, 10, 180000.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 3 MINUTE));


-- =========================================================================
-- 3. INSERT CHI TIẾT VÉ (TICKETS) - Khớp logic loại ghế Standard/VIP
-- =========================================================================
-- Phụ thu: Standard (+0đ), VIP (+15.000đ)

-- Đơn 1: Suất 1, Ghế Standard A1, A2 (70k + 70k)
INSERT INTO tickets (booking_id, showtime_id, seat_id, original_price, discount_amount, final_price)
SELECT 1, 1, id, 70000.00, 0.00, 70000.00 FROM seats WHERE room_id = 1 AND row_index = 'A' AND column_index IN (1, 2);

-- Đơn 2: Suất 1, Ghế VIP D1, D2 (85k + 85k = 170k)
INSERT INTO tickets (booking_id, showtime_id, seat_id, original_price, discount_amount, final_price)
SELECT 2, 1, id, 85000.00, 0.00, 85000.00 FROM seats WHERE room_id = 1 AND row_index = 'D' AND column_index IN (1, 2);

-- Đơn 3: Suất 1, Ghế Standard A3 (Đã hủy)
INSERT INTO tickets (booking_id, showtime_id, seat_id, original_price, discount_amount, final_price)
SELECT 3, 1, id, 70000.00, 0.00, 70000.00 FROM seats WHERE room_id = 1 AND row_index = 'A' AND column_index = 3;

-- Đơn 4: Suất 1, Ghế Standard B1, B2 (Đang chờ)
INSERT INTO tickets (booking_id, showtime_id, seat_id, original_price, discount_amount, final_price)
SELECT 4, 1, id, 70000.00, 0.00, 70000.00 FROM seats WHERE room_id = 1 AND row_index = 'B' AND column_index IN (1, 2);

-- Đơn 5: Suất 2, Ghế Standard A1, A2 (Phòng 2, Giá base 80k)
INSERT INTO tickets (booking_id, showtime_id, seat_id, original_price, discount_amount, final_price)
SELECT 5, 2, id, 80000.00, 0.00, 80000.00 FROM seats WHERE room_id = 2 AND row_index = 'A' AND column_index IN (1, 2);

-- Đơn 6: Suất 2, Ghế VIP D1, D2 (Giá 80k + 15k VIP = 95k/vé)
INSERT INTO tickets (booking_id, showtime_id, seat_id, original_price, discount_amount, final_price)
SELECT 6, 2, id, 95000.00, 0.00, 95000.00 FROM seats WHERE room_id = 2 AND row_index = 'D' AND column_index IN (1, 2);

-- Đơn 7: Suất 3, Ghế Standard A1, A2 (Phòng 3, Giá base 90k)
INSERT INTO tickets (booking_id, showtime_id, seat_id, original_price, discount_amount, final_price)
SELECT 7, 3, id, 90000.00, 0.00, 90000.00 FROM seats WHERE room_id = 3 AND row_index = 'A' AND column_index IN (1, 2);

-- Đơn 8: Suất 4, Ghế Standard A1, A2 (Phòng 4, Giá base 100k)
INSERT INTO tickets (booking_id, showtime_id, seat_id, original_price, discount_amount, final_price)
SELECT 8, 4, id, 100000.00, 0.00, 100000.00 FROM seats WHERE room_id = 4 AND row_index = 'A' AND column_index IN (1, 2);

-- Đơn 9: Suất 4, Ghế VIP D1, D2 (Giá 100k + 15k VIP = 115k/vé)
INSERT INTO tickets (booking_id, showtime_id, seat_id, original_price, discount_amount, final_price)
SELECT 9, 4, id, 115000.00, 0.00, 115000.00 FROM seats WHERE room_id = 4 AND row_index = 'D' AND column_index IN (1, 2);

-- Đơn 10: Suất 6, Ghế Standard A1, A2 (Phòng 7, Giá base 85k)
INSERT INTO tickets (booking_id, showtime_id, seat_id, original_price, discount_amount, final_price)
SELECT 10, 6, id, 85000.00, 0.00, 85000.00 FROM seats WHERE room_id = 7 AND row_index = 'A' AND column_index IN (1, 2);

-- Đơn 12: Suất 8, Ghế Standard A1, A2 (Phòng 9, Giá base 90k)
INSERT INTO tickets (booking_id, showtime_id, seat_id, original_price, discount_amount, final_price)
SELECT 12, 8, id, 90000.00, 0.00, 90000.00 FROM seats WHERE room_id = 9 AND row_index = 'A' AND column_index IN (1, 2);

-- Đơn 13: Suất 8, Ghế VIP D1, D2 (Giá 90k + 15k VIP = 105k/vé)
INSERT INTO tickets (booking_id, showtime_id, seat_id, original_price, discount_amount, final_price)
SELECT 13, 8, id, 105000.00, 0.00, 105000.00 FROM seats WHERE room_id = 9 AND row_index = 'D' AND column_index IN (1, 2);

-- Đơn 14 & 15: Suất 10, Ghế Standard (Phòng 11, Giá base 90k)
INSERT INTO tickets (booking_id, showtime_id, seat_id, original_price, discount_amount, final_price)
SELECT 14, 10, id, 90000.00, 0.00, 90000.00 FROM seats WHERE room_id = 11 AND row_index = 'A' AND column_index IN (1, 2);

INSERT INTO tickets (booking_id, showtime_id, seat_id, original_price, discount_amount, final_price)
SELECT 15, 10, id, 90000.00, 0.00, 90000.00 FROM seats WHERE room_id = 11 AND row_index = 'B' AND column_index IN (1, 2);


-- =========================================================================
-- 4. ĐỒNG BỘ LẠI TRẠNG THÁI GHẾ TRONG SEAT_STATUS THEO ĐƠN HÀNG
-- =========================================================================
-- Ghế đã thanh toán xong -> BOOKED
UPDATE seat_status ss
    JOIN tickets t ON ss.seat_id = t.seat_id AND ss.showtime_id = t.showtime_id
    JOIN bookings b ON t.booking_id = b.id
    SET ss.status = 'BOOKED'
WHERE b.status = 'PAID';

-- Ghế đang chờ thanh toán -> HELD (giữ trong 10 phút)
UPDATE seat_status ss
    JOIN tickets t ON ss.seat_id = t.seat_id AND ss.showtime_id = t.showtime_id
    JOIN bookings b ON t.booking_id = b.id
    SET ss.status = 'HELD', ss.hold_expires_at = DATE_ADD(NOW(), INTERVAL 10 MINUTE)
WHERE b.status = 'PENDING';


-- =========================================================================
-- 5. LỊCH SỬ THANH TOÁN (PAYMENTS)
-- =========================================================================
INSERT INTO payments (booking_id, amount, method, status, transaction_code, paid_at, created_at)
VALUES
    (1, 140000.00, 'momo', 'SUCCESS', 'MOMO_TXN_001', '2026-05-01 14:31:00', '2026-05-01 14:30:00'),
    (2, 170000.00, 'vnpay', 'SUCCESS', 'VNPAY_TXN_002', '2026-05-01 15:02:00', '2026-05-01 15:00:00'),
    (3, 70000.00, 'card', 'FAILED', 'CARD_ERR_109', NULL, '2026-05-01 09:00:00'),
    (4, 140000.00, 'momo', 'PENDING', 'MOMO_PENDING_004', NULL, DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
    (5, 160000.00, 'vnpay', 'SUCCESS', 'VNPAY_TXN_005', '2026-05-02 09:02:00', '2026-05-02 09:00:00'),
    (6, 190000.00, 'momo', 'SUCCESS', 'MOMO_TXN_006', '2026-05-02 09:16:00', '2026-05-02 09:15:00'),
    (7, 180000.00, 'card', 'SUCCESS', 'BANK_TXN_007', '2026-05-02 14:05:00', '2026-05-02 14:00:00'),
    (8, 200000.00, 'vnpay', 'SUCCESS', 'VNPAY_TXN_008', '2026-05-03 17:02:00', '2026-05-03 17:00:00'),
    (9, 230000.00, 'momo', 'SUCCESS', 'MOMO_TXN_009', '2026-05-03 17:17:00', '2026-05-03 17:15:00'),
    (10, 170000.00, 'vnpay', 'SUCCESS', 'VNPAY_TXN_010', '2026-05-04 08:32:00', '2026-05-04 08:30:00'),
    (12, 180000.00, 'momo', 'SUCCESS', 'MOMO_TXN_012', '2026-05-05 21:02:00', '2026-05-05 21:00:00'),
    (13, 210000.00, 'card', 'SUCCESS', 'BANK_TXN_013', '2026-05-05 21:18:00', '2026-05-05 21:15:00'),
    (14, 180000.00, 'vnpay', 'SUCCESS', 'VNPAY_TXN_014', '2026-05-06 18:32:00', '2026-05-06 18:30:00'),
    (15, 180000.00, 'momo', 'PENDING', 'MOMO_PENDING_015', NULL, DATE_SUB(NOW(), INTERVAL 3 MINUTE));


-- =========================================================================
-- 6. BẢNG REVIEW PHIM (Cho xôm tụ)
-- =========================================================================
TRUNCATE TABLE movie_reviews;
INSERT INTO movie_reviews (id, rating, comment_text, created_at, user_id, movie_id)
VALUES
    (1, 5, 'Phim đỉnh chóp, đáng đồng tiền bát gạo', '2026-05-02 12:00:00', 2, 1),
    (2, 4, 'Hơi ngắn nhưng nội dung lôi cuốn', '2026-05-02 13:00:00', 3, 1),
    (3, 5, 'Khóc hết một lít nước mắt phim này :(', '2026-05-03 22:00:00', 4, 2),
    (4, 2, 'Kinh dị gì mà buồn ngủ quá trời.', '2026-05-05 23:50:00', 5, 5);