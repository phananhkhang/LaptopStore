# Quy chuẩn Phát triển Dự án LaptopStore (ECC — Test-Driven Development Mandatory)

Dự án này áp dụng nghiêm ngặt quy trình **TDD (Test-Driven Development)** từ chuẩn **ECC (Everything Claude Code)** kết hợp kiến trúc hệ thống hiện có.

---

## 1. Nguyên tắc cốt lõi: Tests BEFORE Code (Bắt buộc)

Mọi yêu cầu viết tính năng mới, sửa lỗi (bug fix), thêm endpoint API hoặc refactor đều **BẮT BUỘC** tuân thủ chu kỳ 3 bước:

1. **BƯỚC 1 - RED (Viết Test Trước):**
   - Viết bài test tự động mô tả chính xác yêu cầu nghiệp vụ hoặc tái hiện lỗi (Bug reproducer).
   - Chạy test runner của module (`.\backend\mvnw.cmd test -Dtest=...` hoặc `npm test` ở frontend) để **chứng minh test THẤT BẠI (RED)** do thiếu mã xử lý.
   - *Nghiêm cấm sửa đổi code nghiệp vụ trước khi có bài test RED được biên dịch và chạy thực tế.*

2. **BƯỚC 2 - GREEN (Viết Code Tối Thiểu):**
   - Chỉ viết lượng mã tối thiểu (minimal implementation) vừa đủ để bài test chuyển sang màu xanh **(GREEN)**.
   - Chạy lại test suite để xác nhận bài test đã PASS.

3. **BƯỚC 3 - REFACTOR (Làm Sạch & Chuẩn Hóa):**
   - Tối ưu hóa mã nguồn, khử trùng lặp (DRY), tuân thủ Clean Code và quy ước kiến trúc.
   - Chạy lại toàn bộ test liên quan để đảm bảo không phát sinh regression (vẫn giữ GREEN).

---

## 2. Tiêu chuẩn Kiểm thử theo Phân hệ

### Backend (Java / Spring Boot)
- Kích hoạt kỹ năng: `springboot-tdd` & `tdd-workflow`.
- Công cụ: JUnit 5, Mockito, Spring MockMvc, Testcontainers, JaCoCo.
- Tuân thủ quy tắc kiến trúc tại [backend/AGENTS.md](./backend/AGENTS.md) (Controller -> Service contract, JPA không cross-module, Flyway migration).
- Runner: `.\backend\mvnw.cmd test`

### Frontend (React / Vite / TypeScript)
- Kích hoạt kỹ năng: `react-testing` & `tdd-workflow`.
- Công cụ: Vitest, React Testing Library.
- Runner: `npm test` hoặc `npm run test:coverage`.

---

## 3. Yêu cầu Chất lượng (Quality Gate)
- **Độ phủ (Coverage):** Tối thiểu $\ge$ 80% coverage (Unit test + Integration test).
- **Edge Cases:** Bắt buộc kiểm thử các trường hợp biên: `null / undefined`, chuỗi/mảng rỗng, giá trị vượt ngưỡng (min/max), tài khoản không quyền (403 Forbidden), chưa đăng nhập (401 Unauthorized), lỗi ngoại lệ.
- **Báo cáo bằng chứng (Evidence):** Sau mỗi tác vụ, tóm tắt kết quả kiểm thử (RED $\rightarrow$ GREEN) và lệnh kiểm tra đã chạy.
