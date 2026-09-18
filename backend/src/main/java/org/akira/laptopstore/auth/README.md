# Module auth — Xác thực

**Owner đề xuất:** A theo [bảng ownership](../../../../../../../docs/architecture/03-ownership.md). Chưa gán thành viên thật. **Trạng thái:** skeleton hướng dẫn; các class dưới đây chưa triển khai. Thiết kế chi tiết cần được nhóm chốt khi nhận task.

## Trách nhiệm

Đăng ký, đăng nhập, đăng xuất, cấp và xoay refresh token. Không quản lý hồ sơ, địa chỉ hoặc phân vai trò tùy ý.

## Cần viết gì

| Thành phần | Class định hướng |
|---|---|
| Entity | RefreshToken |
| Repository | RefreshTokenRepository |
| Service | AuthService, RefreshTokenService |
| Controller | AuthController |
| Request | RegisterRequest, LoginRequest |
| Response/contract | LoginResponse, RefreshTokenResponse |

Đây là danh sách gợi ý theo use case, không bắt buộc tạo đủ class trước khi có nhu cầu.

## Dữ liệu và phụ thuộc

**Dữ liệu sở hữu:** refresh_tokens.

**Được gọi:** user.service để tạo/tra cứu tài khoản; security.JwtService và PasswordEncoder để xử lý thông tin xác thực. Không gọi repository/entity module khác. Xem [allowlist và contract](../../../../../../../docs/architecture/04-dependencies.md).

## Bất biến và ràng buộc

- Đăng ký luôn cấp USER ở server; bỏ qua hoặc từ chối role do client gửi.
- Refresh token ngẫu nhiên, DB lưu hash; kiểm tra hết hạn, thu hồi và rotation trong transaction có khóa. Token đã dùng không cấp token mới.
- Access token trả qua DTO; refresh token qua cookie HttpOnly theo cấu hình. Không đưa hash mật khẩu hay raw refresh token vào response JSON hoặc log.
- Thông báo đăng nhập thất bại không tiết lộ tài khoản có tồn tại. Login, register và refresh cần giới hạn tần suất khi triển khai.

## Luồng triển khai điển hình

Đọc thông tin qua user.service → kiểm tra mật khẩu/trạng thái → cấp token → lưu refresh token → trả thông tin tối thiểu.

## Bàn giao và kiểm tra

Các ca cần chứng minh: email trùng; sai mật khẩu; tài khoản khóa; hai refresh đồng thời; token hết hạn/thu hồi; logout rồi refresh bị từ chối. Cập nhật API, migration và test module cùng PR; ghi kết quả thật. Owner phối hợp người dùng contract khi đổi input/output/lỗi.

## Quy tắc các tầng và liên kết kiến trúc

Quy tắc và ràng buộc chi tiết cho từng tầng (`controller`, `service`, `repository`, `entity`, `dto`, `mapper`) tuân thủ theo [luật tầng](../../../../../../../docs/architecture/02-structure-and-layers.md) và [luồng nghiệp vụ](../../../../../../../docs/architecture/06-business-flows.md).

