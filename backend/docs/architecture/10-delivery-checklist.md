# Kế hoạch triển khai và tiêu chí hoàn tất

## Các mốc theo luồng sử dụng

| Mốc | Phối hợp | Tiêu chí |
|---|---|---|
| 0 Chốt nền | Cả nhóm; A điều phối | Gán A/B/C/D; chốt phương án thiết kế cần cho sprint; thống nhất contract, migration và role |
| 1 Đăng nhập và catalog | A + B; C/D làm theo mock contract đã thống nhất | USER đăng nhập, xem sản phẩm thật từ DB; ADMIN tạo catalog đúng quyền |
| 2 Giỏ và đơn COD | C + B + A; D chuẩn bị thu COD | Thêm giỏ, checkout atomic, staff xử lý, hủy hoàn tồn đúng một lần |
| 3 Vận hành và mở rộng | B procurement; D payment/coupon/review; C contract đơn | Nhập hàng, ghi thu COD, coupon và review theo điều kiện |
| 4 Online mô phỏng và demo | D + C; cả nhóm kiểm tra | Callback lặp/trễ xử lý đúng; frontend đi được luồng đầy đủ; seed demo tái lập |

Không có deadline cụ thể vì chưa biết lịch nhóm. Không cần chờ mọi API xong mới làm frontend; dựng wireframe tối thiểu và chốt API cho từng mốc.

## Checklist một use case

- [ ] Owner và reviewer xác định; trách nhiệm không chồng lấn.
- [ ] Input/output, role/ownership, lỗi và trạng thái được ghi.
- [ ] Controller mỏng; service xử lý use case; dependency đúng allowlist.
- [ ] DTO không lộ entity/hash/token; client không quyết định giá/quyền/tồn.
- [ ] Migration mới có constraint/index và chạy trên DB đúng phiên bản.
- [ ] Test hành vi quan trọng, thất bại, rollback/concurrency khi liên quan.
- [ ] Idempotency xử lý request/callback lặp khi có tác động tồn/tiền.
- [ ] Ví dụ request/response và README cập nhật; frontend biết thay đổi contract.
- [ ] PR ghi kiểm tra đã chạy và giới hạn; chưa triển khai không được đánh dấu hoàn tất.

## Việc cần nhóm chốt

| Nội dung | Đề xuất hiện tại | Owner điều phối |
|---|---|---|
| Danh tính ownership | A/B/C/D theo tài liệu 03 | Leader |
| Chi tiết tồn/transaction | Tồn khả dụng ở variant, khóa tăng dần theo ID (tài liệu 04, 05) | B + C |
| Token, CORS, CSRF, TTL | Topology frontend, access token Bearer, refresh token trong HttpOnly cookie (tài liệu 08) | A |
| Payment order dependency | Payment gọi order, tách session sau checkout (tài liệu 04, 06) | C + D |
| Response/data convention | DTO trực tiếp và lỗi thống nhất (tài liệu 07) | A + frontend |
| Test/migration tooling | PostgreSQL thực và test theo rủi ro (tài liệu 09) | A + từng owner |
| Phí giao hàng, hoàn tiền | MVP phí 0, hoàn tiền tự động ngoài phạm vi | C + D + leader |

## Rủi ro cụ thể

| Rủi ro | Cách xử lý dự kiến |
|---|---|
| Dùng tutorial Boot 3 cho project Boot 4.1.1 | Kiểm tra dependency/API theo version hiện có trước thêm code |
| Catalog và inventory cùng chạm bảng variant | Chỉ stock contract từ inventory; metadata có version; review chung B/C |
| Double checkout/double receive/callback lặp | Khóa + unique key + test hai transaction |
| Mất dữ liệu tiền khi callback trễ | Lưu payment và cờ đối soát, không hồi sinh đơn đã hủy |
| Tài liệu dài nhưng không được theo | README cạnh source, checklist PR nhỏ, bám sát các tài liệu kiến trúc trọng tâm |
| Nhầm thư mục là code đã có | Tài liệu ghi rõ skeleton và trạng thái hiện có; cập nhật theo từng PR |
