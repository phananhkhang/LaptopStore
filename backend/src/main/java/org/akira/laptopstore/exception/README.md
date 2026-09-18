# exception

**Owner đề xuất:** A; chưa gán tên. **Trạng thái:** mới có hướng dẫn, chưa có class.

## Cần viết

BusinessException, ResourceNotFoundException, GlobalExceptionHandler, ErrorResponse.

Exception cơ sở, mã lỗi dùng chung và ánh xạ lỗi MVC. Exception riêng như InvalidOrderState có thể đặt cạnh service/module sở hữu và kế thừa cơ sở này.

## Ràng buộc

Không phụ thuộc service/repository module; không xử lý bù nghiệp vụ trong handler. Không lộ SQL/stack trace/secret. Tránh handler phải import mọi exception cụ thể: map bằng contract BusinessException/code chung.

## Kiểm tra và phối hợp

400/404/409/422/500 đúng shape; unexpected error không lộ dữ liệu; filter errors do security xử lý cùng schema.

Đọc [ownership](../../../../../../../docs/architecture/03-ownership.md), [dependency](../../../../../../../docs/architecture/04-dependencies.md) và [bảo mật](../../../../../../../docs/architecture/08-security.md).
