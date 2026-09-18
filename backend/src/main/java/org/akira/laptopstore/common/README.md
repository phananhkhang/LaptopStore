# common

**Owner đề xuất:** A; chưa gán tên. **Trạng thái:** mới có hướng dẫn, chưa có class.

## Cần viết

BaseEntity nếu cần, PageResponse, DTO giá trị thật sự dùng chung.

Kiểu kỹ thuật nhỏ dùng ở nhiều module; BaseEntity chỉ dùng cho entity có đúng semantics. Response lỗi thuộc exception. Chưa tạo ApiResponse wrapper.

## Ràng buộc

Không import module nghiệp vụ/security/config/repository. Không để OrderStatus, CouponRule, StockRule hoặc UserRepository trong common. Có một nơi sử dụng chưa đủ lý do đưa class vào đây.

## Kiểm tra và phối hợp

Không kéo vòng phụ thuộc; API chung ổn định; reviewer từ các module sử dụng khi đổi contract.

Đọc [ownership](../../../../../../../docs/architecture/03-ownership.md), [dependency](../../../../../../../docs/architecture/04-dependencies.md) và [bảo mật](../../../../../../../docs/architecture/08-security.md).
