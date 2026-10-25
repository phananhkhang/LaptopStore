<div align="center">
  <img src="https://capsule-render.vercel.app/api?type=waving&color=timeGradient&height=200&section=header&text=BÁO%20CÁO%20TỔNG%20KẾT%20TUẦN%205&fontSize=50&fontColor=ffffff" width="100%"/>
</div>

<div align="center">
  <a href="#"><img src="https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=java&logoColor=white"></a>
  <a href="#"><img src="https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?style=for-the-badge&logo=spring&logoColor=white"></a>
  <a href="#"><img src="https://img.shields.io/badge/PostgreSQL-17-4169E1?style=for-the-badge&logo=postgresql&logoColor=white"></a>
  <a href="#"><img src="https://img.shields.io/badge/Status-100%25_Completed-4CAF50?style=for-the-badge"></a>
</div>
<br>

> 💡 **Nhật ký Kỹ thuật (Engineering Log):** Tài liệu này được biên soạn nhằm lưu trữ tri thức, ghi nhận các quyết định kiến trúc và tổng kết toàn bộ tiến độ của Tuần 5. Tài liệu được thiết kế rõ ràng, minh bạch để các thành viên trong nhóm dễ dàng nắm bắt, tái sử dụng và phục vụ công tác đánh giá của Nhóm trưởng/Giảng viên.

### 📋 THÔNG TIN CHUNG

| Tiêu chí | Thông tin chi tiết |
| :--- | :--- |
| 🧑‍💻 **Họ và tên người thực hiện** | **Huỳnh Thái Kiệt** |
| 🎓 **Mã số sinh viên (MSSV)** | 3124410172 |
| 📚 **Học phần** | Chuyên đề J2EE |
| 👨‍🏫 **Giảng viên hướng dẫn** | ThS. Nguyễn Thanh Phước |
| 🕒 **Lớp / Thời gian** | Sáng thứ 7 (5 tiết) |
| 📅 **Học kỳ / Năm học** | Học kỳ 1 / 2026 - 2027 |
| ⏱️ **Thời gian xuất báo cáo** | 25/10/2026 - 18:15:00 |

---

### 1. TÓM TẮT SƠ LƯỢC VỀ DỰ ÁN
**LaptopStore** là hệ thống thương mại điện tử quy mô lớn được thiết kế theo kiến trúc Modular Monolith. Hệ thống quản lý toàn diện từ khâu nhập hàng (Procurement), kiểm soát tồn kho (Inventory), danh mục sản phẩm (Catalog) cho đến giao diện khách hàng (Storefront). 

### 2. YÊU CẦU TUẦN 5
1. **Backend:** Xây dựng hoàn chỉnh module **Procurement** (Quản lý Nhà cung cấp & Phiếu nhập hàng). Tích hợp logic liên module để khi nhận hàng (`receivePurchaseOrder`), hệ thống tự động gọi sang **Inventory** để cộng tồn kho và sinh nhật ký biến động (`StockMovement`).
2. **Frontend:** Chuyển đổi giao diện Catalog từ Mock Data sang gọi dữ liệu thật qua REST API. Hoàn thiện tính năng chọn biến thể và giỏ hàng (LocalStorage). Thiết lập cấu trúc thư mục cho phân hệ Staff/Admin.
3. **Bảo mật & Kiểm thử:** Đảm bảo toàn vẹn giao dịch (ACID), chống lỗi đa luồng (Race Condition) và hoàn thiện các bài Test (Unit Test, WebMvc, ArchUnit).

### 3. HƯỚNG GIẢI QUYẾT & 4. LÝ DO
*   **Sử dụng `@Lock(LockModeType.PESSIMISTIC_WRITE)`:** Áp dụng khóa bi quan tại tầng Database (`findByIdForUpdate`). 
    *   *Lý do:* Ngăn chặn triệt để lỗi "Lost Update" khi 2 nhân viên cùng nhận hàng hoặc điều chỉnh kho cùng lúc, đảm bảo tồn kho không bị ghi đè sai lệch.
*   **Sử dụng `@Transactional`:** Bao bọc toàn bộ chu trình tạo phiếu và nhận hàng.
    *   *Lý do:* Nếu có lỗi xảy ra ở khâu cộng tồn kho, toàn bộ thay đổi ở phiếu nhập sẽ được Rollback, đảm bảo tính nguyên tử (Atomicity).
*   **Sử dụng Khóa Idempotency động (`System.currentTimeMillis()`):**
    *   *Lý do:* Một phiếu nhập có thể chia làm nhiều đợt nhận. Khóa chống trùng lặp phải sinh động theo thời gian để cho phép nhận hàng đợt 2, đợt 3 mà không bị hệ thống chặn nhầm.

---

### ⚙️ CHI TIẾT CÔNG VIỆC

#### 1. Mục tiêu dự án (Focus Tuần 5)
Hoàn thiện "Trái tim kho bãi" của dự án: Dòng chảy dữ liệu từ việc tạo Phiếu nhập nháp (`DRAFT`) -> Nhận một phần (`PARTIALLY_RECEIVED`) -> Nhận đủ (`RECEIVED`) và thông luồng dữ liệu thật lên giao diện Frontend.

#### 2. Cấu hình & Môi trường
*   **Ngôn ngữ/Framework:** Java 25, Spring Boot 4.1.1, ReactJS (TypeScript, Vite).
*   **Database:** PostgreSQL 17 (Cấu hình cổng `8081` cho backend).
*   **Công cụ:** Maven, npm.

#### 3. Cấu trúc thư mục (Các vùng tác động chính)
```text
📦 LaptopStore
 ┣ 📂 backend/src/main/java/org/akira/laptopstore/
 ┃ ┣ 📂 procurement/ (Entity, Service, Controller cho Supplier & PurchaseOrder)
 ┃ ┣ 📂 inventory/   (Xử lý StockMovement, Idempotency)
 ┃ ┣ 📂 catalog/     (ProductVariant, Khóa bi quan Pessimistic Lock)
 ┃ ┗ 📂 config/      (DatabaseSeeder tự động sinh Mock Data)
 ┗ 📂 frontend/src/modules/
   ┣ 📂 catalog/     (API Clients, ProductListPage, LocalStorage Cart)
   ┗ 📂 procurement/ (Cấu trúc UI phân quyền Staff/Admin)
```

#### 4. Các thành phần chính đã hoàn thiện
*   `Supplier`, `PurchaseOrder`, `PurchaseOrderItem` (Entity & CRUD logic).
*   `InventoryServiceImpl`: Kích hoạt cộng tồn kho và chống trùng lặp đợt nhập hàng.
*   `ProductVariantRepository`: Nâng cấp khóa vật lý bảo vệ tính toàn vẹn tồn kho.
*   `DatabaseSeeder`: Tự động nạp tài khoản ADMIN (mã hóa BCrypt) và dữ liệu sản phẩm mẫu.

#### 5. Các chức năng và URL kiểm thử
*   🔑 **Đăng nhập lấy Token:** `POST /api/v1/auth/login` (Body: `{ "email": "kietadmin@gmail.com", "password": "password123" }`)
*   📝 **Tạo phiếu nhập:** `POST /api/v1/staff/purchase-orders`
*   📦 **Nhận hàng:** `POST /api/v1/staff/purchase-orders/{id}/receive`

#### 6. Các extensions và phần mềm cài đặt thêm
*   **Thunder Client / Postman:** Dùng để test API trực tiếp kèm JWT (Bearer Token).
*   **SQLTools (VSCode) / DBeaver:** Quản trị PostgreSQL trực tiếp, truy vấn kiểm chứng sự biến động của bảng `product_variants` và `stock_movement`.

#### 7. Hướng dẫn khởi chạy DB / Tools
1. Đảm bảo dịch vụ PostgreSQL 17 đang chạy ngầm (`services.msc` trên Windows hoặc systemctl trên Linux).
2. Tạo database trống (Ví dụ: `laptopstore_db`). Spring Boot sẽ tự động tạo bảng (auto-ddl).

#### 8. Hướng dẫn khởi chạy dự án
*   **Backend:** Mở terminal tại thư mục `backend`, gõ `mvn clean install` sau đó chạy `mvn spring-boot:run`.
*   **Frontend:** Mở terminal tại thư mục `frontend`, gõ `npm install`, sau đó chạy `npm run dev`.

#### 9. Kết quả khi khởi chạy
*   Server backend báo `Started LaptopStoreApplication...`. Dòng log báo `✅ Đã khởi tạo thành công tài khoản ADMIN...` và sinh Mock Data.
*   Frontend biên dịch thành công (Zero Compile Errors) và hiển thị danh mục sản phẩm từ Backend.
*   Quá trình Test E2E qua API diễn ra hoàn hảo: Tồn kho cộng chính xác, trạng thái phiếu nhập chuyển tự động.

---

### 📊 THỐNG KÊ & ĐÁNH GIÁ

*   **Số lượng công việc:** ~20 tasks lớn nhỏ (Cả Backend, Frontend, Testing và Vá lỗi).
*   **Hoàn thành:** **20 / 20 (Đạt 100%)**. Không có task nợ (Technical Debt).
*   **Đánh giá mức độ hoàn thành:** **XUẤT SẮC**. Ý thức làm việc cực kỳ tập trung, chủ động phát hiện lỗi kiến trúc (Concurrency) trước khi hệ thống thực sự đi vào vận hành thực tế.

---

### 🚧 KHÓ KHĂN & CÁCH KHẮC PHỤC

| Khó khăn (Bugs) | Quá trình & Cách khắc phục |
| :--- | :--- |
| **Lỗi 1: Nhận hàng đợt 2 không cộng tồn kho** | *Nguyên nhân:* Khóa Idempotency `operationKey` bị trùng lặp vì chỉ dùng `purchaseOrderId` và `variantId`.<br>*Khắc phục:* Bổ sung dấu thời gian `System.currentTimeMillis()` vào chuỗi hash để đảm bảo tính duy nhất cho mỗi đợt nhận. |
| **Lỗi 2: Ghi đè Tồn kho (Lost Update)** | *Nguyên nhân:* Hai nhân viên nhận hàng/điều chỉnh cùng lúc, đọc số cũ, ghi đè số mới làm mất tồn kho.<br>*Khắc phục:* Áp dụng `@Lock(LockModeType.PESSIMISTIC_WRITE)` vào hàm `findByIdForUpdate` trong Repository để khóa vật lý dòng dữ liệu. |
| **Lỗi 3: Spring Boot văng lỗi Circular Bean** | *Nguyên nhân:* Biến `app.order.expire-check-interval-ms` trong `application.yml` tham chiếu vòng lặp vô tận.<br>*Khắc phục:* Chuẩn hóa lại cú pháp khai báo, sử dụng giá trị tĩnh thay vì trỏ ngược lại chính tên biến. |
| **Lỗi 4: Lỗi Data Seeder & JWT Login** | *Nguyên nhân:* Mật khẩu lưu dạng plaintext, và gán Role bằng String vào Entity chứa `Set<Role>`.<br>*Khắc phục:* Sửa Seeder dùng `PasswordEncoder` (BCrypt) và truy vấn DB để gán chính xác object `Role`. |

---

### 🎓 BÀI HỌC RÚT RA & TỔNG KẾT

**1. Bài học rút ra:**
*   **Luôn đa nghi về dữ liệu dùng chung (Concurrency):** Khi làm hệ thống Ecommerce hoặc Quản lý kho, tuyệt đối không được tin tưởng vào các lệnh Update thông thường. Phải có tư duy về Transaction và Database Locking (Bi quan/Lạc quan).
*   **Bảo mật không thể "làm đại":** Không thể tự gõ tay mật khẩu vào Database. Phải có Seeder để chạy qua thuật toán Hash (BCrypt).
*   **Kiểm thử E2E (End-to-End) là chân lý:** Giao diện có thể đánh lừa, nhưng Postman/Thunder Client bắn API gọi thẳng DB thì không biết nói dối.

**2. Nhận định về dự án:**
Hệ thống hiện tại đã sở hữu một lớp Backend cực kỳ vững chắc, đáp ứng chuẩn Enterprise (J2EE). Ranh giới giữa các module được phân định rõ ràng. Frontend đã sẵn sàng cho việc hiển thị và tương tác dữ liệu động. Đồ án hoàn toàn đủ sức đạt điểm cao và chịu tải thực tế tốt.

**3. Tóm tắt và củng cố:**
Tuần 5 là một chặng đường cam go từ việc thao tác CRUD thông thường chuyển sang việc xử lý **Logic nghiệp vụ liên phân hệ**. Chúng ta đã vượt qua được thách thức khó nhất của thương mại điện tử là "Tính toàn vẹn hàng hóa". Nền tảng này là bệ phóng hoàn hảo để tiến vào các Tuần tiếp theo với sự tự tin tuyệt đối.

<div align="center">
  <i>Báo cáo được thực hiện với tinh thần trách nhiệm và lòng say mê công nghệ cao nhất.</i><br>
  <b>Huỳnh Thái Kiệt</b>
</div>