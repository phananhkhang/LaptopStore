# Module user — Người dùng và địa chỉ

**Owner đề xuất:** A theo [bảng ownership](../../../../../../../docs/architecture/03-ownership.md). Chưa gán thành viên thật. **Trạng thái:** skeleton hướng dẫn; các class dưới đây chưa triển khai. Thiết kế chi tiết cần được nhóm chốt khi nhận task.

## Trách nhiệm

Sở hữu tài khoản, vai trò, hồ sơ và địa chỉ. Không phát hành token hoặc xử lý checkout.

## Cần viết gì

| Thành phần | Class định hướng |
|---|---|
| Entity | User, Role, Address |
| Repository | UserRepository, RoleRepository, AddressRepository |
| Service | UserService, AddressService, UserCredentialService |
| Controller | UserController, AddressController, AdminUserController |
| Request | UpdateProfileRequest, CreateAddressRequest, ChangeUserStatusRequest |
| Response/contract | UserResponse, AddressResponse |

Đây là danh sách gợi ý theo use case, không bắt buộc tạo đủ class trước khi có nhu cầu.

## Dữ liệu và phụ thuộc

**Dữ liệu sở hữu:** users, roles, user_roles, addresses.

**Được gọi:** Không gọi module nghiệp vụ khác. Trả DTO nội bộ chuyên dụng cho auth, security và order. Không gọi repository/entity module khác. Xem [allowlist và contract](../../../../../../../docs/architecture/04-dependencies.md).

## Bất biến và ràng buộc

- Chuẩn hóa email và có unique constraint ở DB; mật khẩu chỉ lưu dạng hash.
- Mọi truy vấn địa chỉ cá nhân ràng buộc userId từ principal; addressId hợp lệ chưa đủ chứng minh ownership.
- Chỉ ADMIN đổi role/trạng thái người dùng; DTO sửa profile không chứa role, passwordHash hoặc status.
- UserCredentialService có thể trả hash cho auth/security bằng DTO nội bộ, tuyệt đối không dùng DTO này làm HTTP response.

## Luồng triển khai điển hình

Lấy actorId → tải user/address thuộc actor → kiểm tra trường được sửa → lưu → map response.

## Bàn giao và kiểm tra

Các ca cần chứng minh: email khác hoa thường bị trùng; sửa địa chỉ người khác; mass assignment role; khóa tài khoản ảnh hưởng truy cập. Cập nhật API, migration và test module cùng PR; ghi kết quả thật. Owner phối hợp người dùng contract khi đổi input/output/lỗi.

## Quy tắc các tầng và liên kết kiến trúc

Quy tắc và ràng buộc chi tiết cho từng tầng (`controller`, `service`, `repository`, `entity`, `dto`, `mapper`) tuân thủ theo [luật tầng](../../../../../../../docs/architecture/02-structure-and-layers.md) và [luồng nghiệp vụ](../../../../../../../docs/architecture/06-business-flows.md).

