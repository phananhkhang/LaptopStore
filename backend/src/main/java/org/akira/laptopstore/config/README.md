# config

**Owner đề xuất:** A; chưa gán tên. **Trạng thái:** mới có hướng dẫn, chưa có class.

## Cần viết

CorsProperties, OpenApiConfig, ClockConfig, JacksonConfig khi cần.

Wiring bean/cấu hình kỹ thuật có nhu cầu thật. Đọc biến môi trường qua cấu hình typed khi phù hợp; cấu hình provider riêng ở payment/integration.

## Ràng buộc

Không làm use case trong @Bean/@PostConstruct, không tạo seed admin secret, không đặt SecurityConfig thứ hai. Không biến config thành service locator; không tạo bean chưa dùng.

## Kiểm tra và phối hợp

Profile đúng, property validation, CORS đúng origin, không có cấu hình security cạnh tranh.

Đọc [ownership](../../../../../../../docs/architecture/03-ownership.md), [dependency](../../../../../../../docs/architecture/04-dependencies.md) và [bảo mật](../../../../../../../docs/architecture/08-security.md).
