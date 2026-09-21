# Kiến trúc LaptopStore

**Phạm vi:** backend đồ án Spring Boot, một ứng dụng và một PostgreSQL. **Ngày rà soát:** 2026-09-21. **Người điều phối:** Owner A, chưa gán danh tính. Tài liệu phục vụ lập trình viên và reviewer; README trong source là quy tắc tại vị trí làm việc.

| Tài liệu | Đọc để làm gì |
|---|---|
| [01 Tổng quan](01-overview.md) | Phạm vi, bối cảnh, yêu cầu chất lượng, hiện trạng |
| [02 Cấu trúc và các tầng](02-structure-and-layers.md) | Viết class ở đâu và hướng phụ thuộc trong module |
| [03 Ownership](03-ownership.md) | Chia việc, reviewer, dữ liệu và trách nhiệm bàn giao |
| [04 Phụ thuộc và contract](04-dependencies.md) | Danh sách gọi chéo được phép, DTO nội bộ |
| [05 Dữ liệu và nhất quán](05-data-and-consistency.md) | Bảng, constraint, tiền, tồn, transaction |
| [06 Luồng nghiệp vụ](06-business-flows.md) | Checkout, hủy, nhập hàng, thanh toán, trạng thái |
| [07 API và lỗi](07-api-contracts.md) | Endpoint dự kiến, payload, response và mã lỗi |
| [08 Bảo mật](08-security.md) | Role, ownership, token, thông tin nhạy cảm |
| [09 Kiểm thử và vận hành](09-testing-and-operation.md) | Cách chạy, ca kiểm tra và khoảng trống công cụ |
| [10 Hoàn tất và triển khai](10-delivery-checklist.md) | Các mốc, checklist PR, rủi ro và việc cần chốt |
| [11 Entity model](11-entity-model.md) | Baseline entity/schema trước khi viết Flyway |

## Cách áp dụng

- **Hiện có** là điều đã đọc được trong source/cấu hình gốc; chưa khẳng định runtime đã chạy thành công.
- **Accepted** chỉ dùng cho nền tảng đã có căn cứ từ code/tài liệu/yêu cầu. **Proposed** là thiết kế chi tiết mới đề xuất, chưa được nhóm phê duyệt hoặc triển khai.
- Khi nhóm nhận task, chốt phương án thiết kế đề xuất liên quan trước khi triển khai và ghi trạng thái code riêng. Không tự đổi trạng thái chỉ vì đã có folder.
- README con cụ thể hóa quy tắc tầng/module, không tự cho phép phá quy tắc cha. Có xung đột thì cập nhật quyết định và các nơi tham chiếu trong cùng PR.
- Giữ bộ tài liệu tại đường dẫn chuẩn `backend/docs/architecture/`.

