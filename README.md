# Auth Service Platform 🚀

Một nền tảng xác thực và phân quyền đa người dùng (multi-tenant) mạnh mẽ, được xây dựng bằng Spring Boot. Dự án này được thiết kế để trở thành một dịch vụ backend độc lập, có thể tái sử dụng để quản lý người dùng cho nhiều ứng dụng khác nhau.

---

## ✨ Tính năng nổi bật

* **Kiến trúc Đa người dùng (Multi-Tenant):**
    * **Owner (Chủ nền tảng):** Có thể đăng ký, đăng nhập, và quản lý các project của riêng mình.
    * **Project:** Mỗi Owner có thể tạo nhiều Project. Mỗi Project là một không gian độc lập cho một ứng dụng, có `apiKey` riêng.

* **Xác thực cho Người dùng cuối (End-User):**
    * Người dùng cuối có thể đăng ký và đăng nhập vào một Project cụ thể.
    * Luồng xác thực email tùy chỉnh sử dụng JWT có thời gian hết hạn ngắn.

* **Bảo mật Toàn diện:**
    * Xác thực dựa trên JWT cho cả `Owner` và `EndUser`.
    * Tự động vô hiệu hóa token đăng nhập cũ ngay sau khi người dùng đổi mật khẩu.
    * Phân quyền dựa trên vai trò (RBAC) mạnh mẽ:
        * `Owner` quản lý Project của mình.
        * `EndUser` có vai trò `ADMIN` trong một Project có thể quản lý các `EndUser` khác trong cùng Project đó.

* **Khả năng Quản lý Đầy đủ:**
    * **Dành cho Owner:** Quản lý thông tin cá nhân, CRUD cho `Project`, CRUD cho `ProjectRole` theo từng Project, xem danh sách và quản lý `EndUser` (khóa/mở khóa, gán vai trò).
    * **Dành cho End-User:** Quản lý thông tin cá nhân, tự đổi mật khẩu.

---

## 🛠️ Công nghệ sử dụng

* **Backend:** Java 17, Spring Boot 3.x
* **Bảo mật:** Spring Security 6.x
* **Database:** Spring Data JPA / Hibernate, PostgreSQL
* **Xác thực:** JSON Web Token (JWT) - thư viện `jjwt`
* **Build Tool:** Maven
* **Containerization:** Docker

---

## ⚙️ Hướng dẫn Cài đặt & Chạy Local

#### **Điều kiện cần có:**
* JDK 17 trở lên
* Maven 3.6+
* Docker

#### **Các bước thực hiện:**

1.  **Clone repository:**
    ```bash
    git clone [https://github.com/your-username/auth-service-platform.git](https://github.com/your-username/auth-service-platform.git)
    cd auth-service-platform
    ```

2.  **Khởi động Database PostgreSQL bằng Docker:**
    ```bash
    docker run --name my-auth-postgres \
        -e POSTGRES_USER=postgres \
        -e POSTGRES_PASSWORD=123 \
        -e POSTGRES_DB=auth_platform_db \
        -p 5433:5432 \
        -d postgres
    ```

3.  **Cấu hình Biến môi trường:**
    Tạo các biến môi trường trong IDE (ví dụ: IntelliJ Run Configuration) hoặc trong terminal của bạn. Xem danh sách biến ở dưới.

4.  **Chạy ứng dụng:**
    ```bash
    ./mvnw spring-boot:run
    ```
    Ứng dụng sẽ chạy tại `http://localhost:8080`.

---

### **Biến môi trường cần thiết (`Environment Variables`)**

| Biến                 | Mô tả                                       | Ví dụ                                                |
| :------------------- | :------------------------------------------ | :--------------------------------------------------- |
| `DB_URL`             | Chuỗi kết nối JDBC đến database PostgreSQL   | `jdbc:postgresql://localhost:5433/auth_platform_db`  |
| `DB_USERNAME`        | Tên đăng nhập database                      | `postgres`                                           |
| `DB_PASSWORD`        | Mật khẩu database                           | `123`                                                |
| `GMAIL_APP_PASSWORD` | Mật khẩu ứng dụng 16 ký tự từ tài khoản Google | `abczyxwvutsrqpon`                                   |
| `JWT_SECRET`         | Chuỗi bí mật rất dài để ký JWT               | `DayLaMotChuoiBiMatRatDaiVaPhucTapMaKhongAiCoThe...` |

---

## 📜 Tổng quan API

(Đây là danh sách các API chính, chi tiết xem trong code)

| Phương thức | Đường dẫn                                         | Mô tả                                      | Yêu cầu Auth |
| :---------- | :------------------------------------------------- | :----------------------------------------- | :------------ |
| **Owner Auth** |
| `POST`      | `/api/platform/auth/register`                      | Đăng ký Owner mới.                         | Public        |
| `POST`      | `/api/platform/auth/login`                         | Đăng nhập Owner.                           | Public        |
| `GET`       | `/api/platform/auth/verify-email`                  | Xác thực email của Owner.                  | Public        |
| **Project Management** |
| `POST`      | `/api/projects`                                    | Owner tạo Project mới.                     | Owner         |
| `GET`       | `/api/projects`                                    | Owner lấy danh sách Project của mình.      | Owner         |
| `PUT`       | `/api/projects/{projectId}`                        | Owner cập nhật Project.                    | Owner         |
| **End-User Auth** |
| `POST`      | `/api/p/{apiKey}/auth/register`                    | End-User đăng ký vào một Project.          | Public        |
| `POST`      | `/api/p/{apiKey}/auth/login`                       | End-User đăng nhập vào Project.            | Public        |
| `GET`       | `/api/p/{apiKey}/auth/verify-email`                | Xác thực email của End-User.               | Public        |
| **End-User Profile** |
| `GET`       | `/api/eu/me`                                       | End-User lấy thông tin cá nhân.            | End-User      |
| `PUT`       | `/api/eu/me/password`                              | End-User tự đổi mật khẩu.                  | End-User      |
| **Management APIs** |
| `GET`       | `/api/projects/{projectId}/endusers`               | Lấy danh sách End-User của một Project.    | Owner/Admin   |
| `POST`      | `/api/projects/{projectId}/endusers/{userId}/lock` | Khóa một End-User.                         | Owner/Admin   |
| `POST`      | `/api/projects/{projectId}/roles`                  | Tạo một Role mới cho Project.              | Owner/Admin   |

---

## 🚀 Triển khai (Deployment)

Dự án đã bao gồm một `Dockerfile` sử dụng multi-stage build để tạo ra một image gọn nhẹ và tối ưu.

Dự án được thiết kế để dễ dàng triển khai lên các nền tảng như **Render**, Heroku, hoặc bất kỳ dịch vụ nào hỗ trợ Docker container.

---

## 📄 Giấy phép (License)

Dự án này được cấp phép dưới Giấy phép MIT. Xem file `LICENSE` để biết thêm chi tiết.