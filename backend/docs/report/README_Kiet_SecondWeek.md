<div align="center">
  <h1 style="color: #FFB7B2; font-weight: bold; border-bottom: 2px solid #FFDAC1; padding-bottom: 10px;">📊 BÁO CÁO TỔNG KẾT TUẦN 2 </h1>
  <p style="color: #C7CEEA; font-style: italic;">"Làm việc hết sức, debug hết mình, báo cáo tận tâm"</p>
</div>

<br>

### 👤 THÔNG TIN CÁ NHÂN & HỌC PHẦN

| Tiêu chí | Thông tin chi tiết |
| :--- | :--- |
| <b style="color: #B5EAD7;">Họ và tên</b> | Huỳnh Thái Kiệt |
| <b style="color: #B5EAD7;">MSSV</b> | 3124410172 |
| <b style="color: #B5EAD7;">Học phần</b> | Chuyên đề J2EE |
| <b style="color: #B5EAD7;">Giảng viên</b> | ThS. Nguyễn Thanh Phước |
| <b style="color: #B5EAD7;">Lớp</b> | Sáng thứ 7 (5 tiết) |
| <b style="color: #B5EAD7;">Học kỳ / Năm học</b> | HK1 / 2026-2027 |
| <b style="color: #B5EAD7;">Thời gian xuất báo cáo</b> | 02/10/2026 - 13:02:15 |

---

### 📝 1. TÓM TẮT SƠ LƯỢC VỀ DỰ ÁN
* **Tên dự án:** LaptopStore (Hệ thống thương mại điện tử chuyên cung cấp thiết bị công nghệ).
* **Kiến trúc:** Ứng dụng Fullstack tách biệt Frontend (ReactJS/Vite/Tailwind) và Backend (Java Spring Boot RESTful API).
* **Mục tiêu Tuần 2:** Thiết lập môi trường và hoàn thiện luồng dữ liệu (End-to-End) cho tính năng quản lý danh mục sản phẩm (Admin Catalog CRUD).

### 🎯 2. YÊU CẦU & HƯỚNG GIẢI QUYẾT

<details open>
<summary><b style="color: #FFDAC1; font-size: 1.1em;">🔍 Chi tiết Yêu cầu và Hướng giải quyết</b></summary>

* **Yêu cầu:** 
  1. Cấu hình thành công môi trường dev cho cả nhóm (chạy được React, Spring Boot và Database).
  2. Bắn API tạo mới sản phẩm và hiển thị thành công trên giao diện Admin.
* **Hướng giải quyết:** 
  1. Chuyển đổi môi trường từ IntelliJ sang **VS Code** để nhẹ máy và tránh lỗi bản quyền/plugin. Sử dụng **Thunder Client** ngay trong VS Code để test API thay vì cài Postman nặng nề.
  2. Khởi tạo Database PostgreSQL qua **Docker** để đồng bộ môi trường cho cả nhóm.
* **Lý do:** Tối ưu hóa hiệu suất làm việc, giúp các thành viên (đang là sinh viên năm 3) dễ dàng clone project về chạy thử mà không tốn quá 5 phút cài đặt phần mềm.
</details>

---

### ⚙️ 3. CHI TIẾT CÔNG VIỆC THỰC HIỆN

| STT | Hạng mục | Mô tả chi tiết |
| :---: | :--- | :--- |
| **1** | <b style="color: #C7CEEA;">Mục tiêu dự án</b> | Code thành công API tạo sản phẩm, vượt qua bộ lọc bảo mật, lưu xuống DB và render lên bảng quản lý của React. |
| **2** | <b style="color: #C7CEEA;">Môi trường</b> | `Java 25`, `Spring Boot 4.1.1`, `PostgreSQL 17`, `Node.js` (cho Frontend). |
| **3** | <b style="color: #C7CEEA;">Cấu trúc thư mục</b> | Áp dụng chuẩn MVC chia layer: `controller`, `service`, `repository`, `entity`, `dto` (Nằm trong module `org.akira.laptopstore.catalog`). |
| **4** | <b style="color: #C7CEEA;">Thành phần chính</b> | `Product`, `ProductVariant` (chứa SKU và giá), `Category`, `Brand`, `SecurityConfig`. |
| **5** | <b style="color: #C7CEEA;">URL kiểm thử</b> | Bắn JSON vào `POST http://localhost:8080/api/v1/admin/products` và `GET` để lấy danh sách. |
| **6** | <b style="color: #C7CEEA;">Phần mềm/Extensions</b> | Cài đặt **Docker Desktop** (quản lý container DB) và **Thunder Client** (VS Code extension). |
| **7** | <b style="color: #C7CEEA;">Chạy Database</b> | Mở terminal gõ: `docker compose up -d`. Truy cập psql để chèn dữ liệu mồi: `docker compose exec postgres psql -U postgres -d laptop_store`. |
| **8** | <b style="color: #C7CEEA;">Khởi chạy dự án</b> | <b>Frontend:</b> `npm run dev` (Port 5173). <br><b>Backend:</b> `.\mvnw clean spring-boot:run` (Port 8080). |
| **9** | <b style="color: #C7CEEA;">Kết quả</b> | Thunder Client trả về HTTP Status `201 Created`. Frontend React hiển thị đúng dòng "Laptop ThinkPad T14" trạng thái ACTIVE. |

---

### 📈 4. THỐNG KÊ & BIỂU ĐỒ TIẾN ĐỘ

<div align="center">
  *(Tiền bối note: Em hãy chèn các hình ảnh biểu đồ mà anh đã xuất ra vào thư mục `assets` và dùng đường dẫn dưới đây nhé)*
  <br>
  <img src="./assets/pie_chart.png" width="45%" alt="Phân bổ thời gian">
  <img src="./assets/bar_chart.png" width="45%" alt="Thống kê Bug">
  <br>
  <img src="./assets/line_chart.png" width="45%" alt="Tiến độ hoàn thành">
  <img src="./assets/area_chart.png" width="45%" alt="Năng lượng và Stress">
</div>

* **Số lượng công việc:** Đã hoàn thành 100% mục tiêu CRUD Catalog Admin đề ra cho tuần 2.
* **Đánh giá mức độ hoàn thành:** Tốt. Ý thức làm việc cực kỳ tập trung, chủ động đi tìm lỗi trong bảng log thay vì bỏ cuộc khi Server Crash.

---

### 🛠 5. KHÓ KHĂN, CÁCH KHẮC PHỤC & BÀI HỌC

<blockquote style="border-left: 4px solid #FFB7B2; background-color: rgba(255, 183, 178, 0.1); padding: 10px;">
<b style="color: #FFB7B2;">🚨 Khó khăn 1: Ứng dụng sập do lệch cấu hình Password Database</b><br>
<b>Quá trình:</b> Spring Boot báo lỗi <code>FATAL: password authentication failed for user "postgres"</code>.<br>
<b>Khắc phục:</b> Kiểm tra lại file <code>application.properties</code>, đồng bộ mật khẩu, xóa volume Docker cũ (<code>docker compose down -v</code>) và build lại.
</blockquote>

<blockquote style="border-left: 4px solid #FFDAC1; background-color: rgba(255, 218, 193, 0.1); padding: 10px;">
<b style="color: #FFDAC1;">🚨 Khó khăn 2: Lỗi No property 'sku' found for type 'Product'</b><br>
<b>Quá trình:</b> Đặt nhầm hàm <code>existsBySku</code> vào <code>ProductRepository</code> trong khi trường SKU nằm ở bảng <code>ProductVariant</code>.<br>
<b>Khắc phục:</b> Xóa hàm bên <code>ProductRepository</code>, chuyển sang <code>ProductVariantRepository</code>, cập nhật lại <code>ProductServiceImpl</code> và bắt buộc chạy lệnh <code>.\mvnw clean spring-boot:run</code> để xóa cache.
</blockquote>

<blockquote style="border-left: 4px solid #E2F0CB; background-color: rgba(226, 240, 203, 0.1); padding: 10px;">
<b style="color: #E2F0CB;">🚨 Khó khăn 3: Bị Spring Security chặn (Lỗi 401 & 404)</b><br>
<b>Quá trình:</b> API trả về <code>401 UNAUTHENTICATED</code> do yêu cầu JWT Token, sau đó cài nhầm vị trí <code>requestMatchers</code> gây lỗi biên dịch.<br>
<b>Khắc phục:</b> Mở file <code>SecurityConfig.java</code>, đưa lệnh <code>.requestMatchers("/api/v1/admin/products").permitAll()</code> lên trên cùng (ngay trên <code>anyRequest()</code>) để bypass bảo mật tạm thời phục vụ kiểm thử frontend.
</blockquote>

#### 💡 Bài học rút ra (Dành cho bản thân & Các bạn trong nhóm):
1. **Đọc Log từ dưới lên:** Hung thủ thực sự của các lỗi Java/Spring Boot luôn nằm ở dòng `Caused by` cuối cùng trong Terminal.
2. **Hiểu rõ ranh giới Entity:** Cần nắm chắc Sơ đồ ERD. `Product` chỉ chứa thông tin chung, `ProductVariant` mới chứa `SKU` và `Price`.
3. **Phân quyền cái phễu:** Trong cấu hình Spring Security, các Rule cụ thể phải nằm trên cùng, Rule tổng quát (`anyRequest`) bắt buộc nằm dưới cùng.

---

### ✨ 6. NHẬN ĐỊNH DỰ ÁN & CỦNG CỐ KIẾN THỨC
* **Nhận định:** Kiến trúc phân lớp của dự án đang hoạt động rất tốt, luồng dữ liệu đi qua Controller -> Service -> Repository được tổ chức chặt chẽ. Cơ chế quản lý Database bằng Flyway + Docker rất chuyên nghiệp.
* **Củng cố kiến thức:** 
  * Nắm vững cách thiết lập một môi trường làm việc Fullstack từ con số 0.
  * Hiểu sâu về cách Dependency Injection (DI) hoạt động trong Spring Boot thông qua việc sửa lỗi *UnsatisfiedDependencyException*.
  * Củng cố kỹ năng Inspect Frontend (F12 -> Tab Network) để bắt mạch lỗi kết nối giữa ReactJS và Backend.