# --- Giai đoạn 1: Build ứng dụng ---
FROM eclipse-temurin:17-jdk-focal AS build

WORKDIR /app

# Sao chép các file cần thiết để tải dependency trước
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
# Tối ưu hóa bằng cách tải dependency trước, tận dụng cache của Docker
RUN ./mvnw dependency:go-offline

# Sao chép toàn bộ source code và build ra file JAR
COPY src ./src
RUN ./mvnw clean install -DskipTests


# --- Giai đoạn 2: Chạy ứng dụng ---
FROM eclipse-temurin:17-jre-focal

WORKDIR /app

# Lấy file JAR đã được build từ giai đoạn 1
COPY --from=build /app/target/*.jar app.jar

# Mở cổng 8080 để thế giới bên ngoài có thể truy cập
EXPOSE 8080

# Lệnh để khởi động ứng dụng khi container chạy
ENTRYPOINT ["java", "-jar", "app.jar"]