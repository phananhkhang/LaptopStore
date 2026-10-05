<div align="center">
  <img src="https://img.icons8.com/clouds/200/000000/laptop.png" alt="LaptopStore Logo"/>
  <h1>🌟 BÁO CÁO TỔNG KẾT NHIỆM VỤ TUẦN 2 🌟</h1>
</div>

---

## 📋 THÔNG TIN CHUNG

> **Lưu ý dành cho Team:** Đây là báo cáo chi tiết tiến độ tuần 2. Các bạn thành viên vui lòng đọc kỹ phần *Chi tiết công việc* và *URL Kiểm thử* để tích hợp Frontend cho chính xác nhé!

| 🏷️ Danh mục | 📝 Chi tiết |
| :--- | :--- |
| **Họ và tên người thực hiện** | **Huỳnh Thái Kiệt** |
| **MSSV** | **3124410172** |
| **Học phần** | Chuyên đề J2EE |
| **Giảng viên hướng dẫn** | ThS. Nguyễn Thanh Phước |
| **Lớp học** | Sáng thứ 7 (5 tiết) |
| **Học kỳ - Năm học** | Học kỳ 1 \| 2026-2027 |
| **Thời gian chốt báo cáo** | Ngày 05/10/2026 - 23:29:02 |

---

## 🚀 TỔNG QUAN NHIỆM VỤ

### 1. Tóm tắt sơ lược về dự án
Trong tuần 2, nhiệm vụ trọng tâm là hoàn thiện **Lõi Quản lý Danh mục (Catalog Management)** bao gồm 4 module cốt lõi: `Product`, `ProductVariant`, `Brand`, và `Category`. Đồng thời, thiết lập lớp giáp bảo mật (Spring Security) để phân quyền Admin/Public và quy chuẩn hóa luồng làm việc với Git/Flyway Database Migration.

### 2. Yêu cầu
- Hoàn thiện các API CRUD cho các Entity.
- Áp dụng triệt để cơ chế **Soft Delete (Xóa mềm)** để bảo vệ toàn vẹn khóa ngoại (Foreign Key).
- Validate chặt chẽ dữ liệu đầu vào (chống trùng lặp SKU/Name, chống giá trị âm/rỗng).
- Tách bạch rõ ràng Public API (không cần token) và Admin API (bắt buộc role ADMIN).
- Dọn rác Git và quản lý lịch sử commit (Rebase) an toàn trước khi Merge.

### 3. Hướng giải quyết
- Sử dụng **Spring Data JPA** kết hợp `@SQLDelete` và `@SQLRestriction` để xử lý Soft Delete. Trong các trường hợp đặc thù (update trạng thái), dùng **Native Query** để vượt qua bộ lọc tàng hình.
- Dùng **MapStruct** để tự động hóa việc ánh xạ giữa `Entity` và `Response DTO`, tuyệt đối không trả Entity trực tiếp ra API.
- Tận dụng **Bean Validation (@Valid)** ngay tại tầng Controller.
- Cấu hình lại `SecurityFilterChain` theo nguyên tắc *First-Match-Wins* để bít lỗ hổng bypass.

### 4. Lý do
- Việc dùng Soft Delete giúp các hóa đơn/giỏ hàng cũ không bị lỗi khi một sản phẩm ngưng kinh doanh.
- Tách DTO giúp ẩn đi cấu trúc thật của Database, tối ưu băng thông và bảo mật.
- Kiểm tra toàn vẹn dữ liệu (Integration Validate) ở tầng Service giúp Frontend nhận được lỗi HTTP 400/404 rõ ràng thay vì lỗi HTTP 500 mơ hồ từ Database.

---

## 🛠️ CHI TIẾT CÔNG VIỆC

<details open>
<summary><b> Nhấn để xem chi tiết (Mở rộng)</b></summary>

| Hạng mục | Chi tiết triển khai |
| :--- | :--- |
| **1. Mục tiêu dự án** | Xây dựng API vững chắc, không ném lỗi 500, sẵn sàng cho team Frontend gọi dữ liệu. |
| **2. Cấu hình & Môi trường** | ☕ Java 25<br>🍃 Spring Boot 4.1.1<br>🐘 PostgreSQL 17<br>🦅 Flyway (Database Migration) |
| **3. Cấu trúc thư mục** | Tổ chức theo chuẩn Layered Architecture:<br>- `controller/` (Chia làm Public và Admin Controller)<br>- `service/` & `service/impl/`<br>- `repository/`<br>- `dto/request/` & `dto/response/`<br>- `mapper/` |
| **4. Các thành phần chính** | - Module **Category** & **Brand** (Chống trùng tên).<br>- Module **Product** (Chứa khóa ngoại Brand/Category).<br>- Module **ProductVariant** (Quản lý SKU độc nhất, tồn kho, giá). |
| **5. Các chức năng & URL** | **Admin API (Cần Token):**<br>- `POST/PUT/DELETE /api/v1/admin/brands`<br>- `POST/PUT/DELETE /api/v1/admin/categories`<br>- `POST/PUT /api/v1/admin/products/{id}`<br>- `PATCH /api/v1/admin/products/{pId}/variants/{vId}/status`<br><br>**Public API (Không Token):**<br>- `GET /api/v1/brands`, `/api/v1/categories` |
| **6. Extensions cài đặt** | - **Postman**: Test API.<br>- **SQLTools (VSCode) / DBeaver**: Truy vấn trực tiếp PostgreSQL.<br>- **MapStruct Plugin**: Hỗ trợ sinh code ánh xạ. |
| **7. HD chạy Database** | 1. Tạo DB trên PostgreSQL: `CREATE DATABASE laptop_store;`<br>2. Ứng dụng sẽ tự động chạy script `V1__...sql`; `V2__...sql` qua Flyway để cập nhật bảng khi khởi động. |
| **8. HD khởi chạy dự án** | 1. Mở Terminal tại root project.<br>2. Chạy lệnh: `./mvnw spring-boot:run` (hoặc nhấn nút Run trên IDE). |
| **9. Kết quả khởi chạy** | - ✅ Pass 100% test case (Create, Duplicate Check, Soft Delete, Active/Deactivate).<br>- ✅ Lớp bảo mật trả về `401/403` đúng kịch bản khi thiếu Token Admin. |

</details>

---

## 📊 THỐNG KÊ & ĐÁNH GIÁ

- **Số lượng công việc:** 
  - Đã hoàn thành: **6/6 task** (100% - Bao gồm Product, Variant, Brand, Category, Security, Git/Merge).
  - Chưa hoàn thành: **0**.
- **Đánh giá mức độ hoàn thành:** 
  - **Công việc:** Đạt mức Xuất sắc. Các luồng xử lý edge-case (lỗi biên) như trùng SKU, giá âm, tàng hình dữ liệu đều được xử lý mượt mà.
  - **Ý thức:** Cao. Tuân thủ nghiêm ngặt quy tắc dọn rác `.gitignore` và Rebase lịch sử Git trước khi tạo Pull Request, không gây ảnh hưởng code của team.

---

## 🚧 KHÓ KHĂN & CÁCH KHẮC PHỤC

| 🛑 Khó khăn gặp phải | 💡 Quá trình & Cách khắc phục |
| :--- | :--- |
| Lỗi `404 Not Found` khi dùng JPA `findById` để kích hoạt lại Variant đã bị tắt. | Nguyên nhân do `@SQLRestriction("active = true")` tạo "áo tàng hình". Khắc phục bằng cách viết **Native Query** `UPDATE ... WHERE id = ?` tóm trực tiếp vào DB, bỏ qua cơ chế lọc của Hibernate. |
| Bypass bảo mật do cấu hình `SecurityFilterChain` sai thứ tự. | Nhận ra nguyên tắc **First-Match-Wins**. Đã gỡ bỏ cờ `.permitAll()` thừa thãi đặt trên luồng `.hasRole("ADMIN")` và khóa chặt endpoint. |
| Nguy cơ sập Database (Lỗi 500) khi truyền ID Brand/Category ảo. | Viết thêm logic kiểm tra `Integration Validate` tại tầng Service. Chặn ngay bằng lỗi HTTP 400/404 thân thiện trước khi map vào Entity. |
| Xung đột Checksum của Flyway. | Tuyệt đối không sửa file Migration cũ (`V1, V2`). Tạo file mới `V4__add_active_columns_to_catalog.sql` để đảm bảo tính đồng nhất trên mọi máy thành viên. |

---

## 🎓 BÀI HỌC & NHẬN ĐỊNH

### 1. Bài học rút ra
- **Security:** "Token một khi phát ra là bất biến" - Thay đổi quyền dưới DB thì phải đăng nhập lấy token mới.
- **Git Flow:** Không bao giờ push code khi chưa `git fetch` và `git rebase origin/main`. Dọn rác IDE trước khi commit là lịch sự với đồng nghiệp.
- **Data Integrity:** "Luôn nghi ngờ dữ liệu từ Frontend gửi xuống". Sử dụng `@Valid` là chưa đủ, phải kiểm tra tính toàn vẹn quan hệ (Khóa ngoại) ở tầng Service.

### 2. Nhận định về dự án
Hệ thống đang được xây dựng trên một bộ khung kiến trúc cực kỳ chuẩn mực của J2EE. Việc phân tách rạch ròi giữa Public và Admin API giúp dự án có khả năng mở rộng (Scale) rất tốt sau này, dễ dàng tích hợp thêm các cổng thanh toán hoặc hệ thống quản lý kho độc lập.

### 3. Tóm tắt và củng cố kiến thức
Tuần này đã củng cố mạnh mẽ các trụ cột của một Backend Developer:
1. **Thiết kế CSDL & JPA:** Nắm vững `@SQLDelete`, `@SQLRestriction`, và Native Query.
2. **Spring Validation:** Ứng dụng mượt mà `jakarta.validation.constraints`.
3. **Bảo mật:** Làm chủ `SecurityFilterChain`, phân quyền dựa trên JWT.
4. **DevOps Cơ bản:** Quản trị phiên bản Database với Flyway, quản trị mã nguồn sạch với Git Rebase.

---
<div align="center">
  <i>Báo cáo được thực hiện với sự tận tâm và cam kết cao nhất. Sẵn sàng nhận feedback từ Nhóm trưởng! 🚀</i>
</div>