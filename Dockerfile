# ==========================================
# Stage 1: Build stage với Gradle & JDK 23
# ==========================================
FROM eclipse-temurin:23-jdk-alpine AS build

WORKDIR /app

# Copy gradle wrapper và file cấu hình build
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./

# Chuẩn hóa ký tự xuống dòng Windows (CRLF -> LF) và cấp quyền thực thi cho gradlew
RUN sed -i 's/\r$//' ./gradlew && chmod +x ./gradlew

# Tải trước dependencies để tận dụng Docker layer cache
RUN ./gradlew dependencies --no-daemon || true

# Copy toàn bộ source code và build file JAR
COPY src/ src/
RUN ./gradlew bootJar -x test --no-daemon && \
    cp build/libs/$(ls build/libs | grep -v 'plain' | head -n 1) app.jar

# ==========================================
# Stage 2: Runtime environment với JRE 23 siêu nhẹ
# ==========================================
FROM eclipse-temurin:23-jre-alpine

WORKDIR /app

# Chạy dưới user không có quyền root để bảo mật
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy file jar từ build stage sang
COPY --from=build --chown=appuser:appgroup /app/app.jar app.jar

# Mở cổng 8080 của ứng dụng
EXPOSE 8080

# Cấu hình tối ưu bộ nhớ JVM
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# Khởi chạy ứng dụng
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
