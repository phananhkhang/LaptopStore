# Cấu trúc thư mục và quy tắc các tầng

## Cấu trúc đã tạo

Base path: `backend/src/main/java/org/akira/laptopstore/`.

| Package | Các thư mục con |
|---|---|
| `auth`, `user`, `catalog`, `cart`, `order` | controller, service, repository, entity, dto/request, dto/response, mapper |
| `payment` | Các tầng trên và integration |
| `promotion`, `inventory`, `procurement`, `review` | controller, service, repository, entity, dto/request, dto/response, mapper |
| `security` | Filter, JWT, principal và SecurityConfig khi triển khai |
| `exception` | Exception chung và HTTP error handling |
| `config` | Bean/cấu hình ngoài security |
| `common` | DTO chung và `constant` thật sự dùng chung |

Mỗi module có `README.md` tổng với trách nhiệm, nội dung cần viết, ràng buộc và kiểm tra; quy tắc chi tiết cho từng tầng con được quy định tập trung trong tài liệu này. Các thư mục namespace `org/akira` chỉ tạo package path, không phải miền ownership. Không tạo `admin/catalog/service` và `user/catalog/service` trùng nghiệp vụ; có thể tách controller admin/public trong cùng module.

`src/test/java/org/akira/laptopstore/` có thư mục đối xứng theo module, thêm `integration` và `architecture`. Resources có `db/migration`, `static`, `templates`; hai thư mục cuối dành sẵn từ project gốc, backend REST không triển khai React hoặc view server tại đó.

## Luật theo tầng

| Tầng | Được viết/phụ thuộc | Không được làm |
|---|---|---|
| controller | Mapping HTTP, DTO, validation, principal, Service cùng module | Inject repository, tính giá, đổi entity trực tiếp, mở transaction nghiệp vụ |
| service | Use case, quyền sở hữu, transaction, repository cùng module, contract chéo được phép | Phụ thuộc controller, trả entity qua contract chéo, gọi repo khác module |
| repository | JPA query/projection, khóa, persistence của module | Quyết định chuyển trạng thái hoặc gọi service/HTTP |
| entity | Mapping bảng, enum/value rule cục bộ | Inject service/repository, dùng request/response, serialize ra API |
| dto/request | Dữ liệu client được phép gửi, constraint hình dạng | Chứa role/price/owner do server quyết định ngoài endpoint đặc quyền đã duyệt |
| dto/response | DTO HTTP; record snapshot nội bộ ghi rõ phạm vi | Lộ hash, raw token, entity/lazy collection |
| mapper | Biến đổi thuần, whitelist trường | Gọi DB/HTTP, kiểm tra quyền, tự gán tồn/role/payment status |
| integration | Adapter HTTP provider của module | Quyết định PAID, đổi order/stock hoặc lộ DTO provider ra API |

## Quy ước code

- Class PascalCase, method/field camelCase, package lowercase; tên có ý nghĩa nghiệp vụ. Dùng `Order` với bảng `orders`, tránh tên SQL dành riêng.
- Interface `ProductService` và implementation `ProductServiceImpl` cùng `service/` nếu cần hợp đồng ổn định. Không thêm usecase/port/adapter/domain/application chỉ để giống kiến trúc khác.
- Constructor injection, dependency bắt buộc rõ; không dùng static service locator hoặc `@Lazy` để che vòng phụ thuộc.
- Bean Validation kiểm tra hình dạng request; service kiểm tra nghiệp vụ; constraint DB bảo vệ cạnh tranh. Chưa có dependency validation thì thêm đúng task, không giả định annotation đã hoạt động.
- DTO nội bộ như `VariantSnapshot`, `OrderPaymentSnapshot` đặt trong `dto/response/`, ghi rõ “internal contract”; HTTP controller không được tự tiện trả mọi DTO ở đây.
- Entity chỉ quan hệ JPA trong module; liên kết module khác dùng ID và FK qua migration. Không cascade delete xuyên module.
- Response mapping hoàn tất trong service/transaction đọc để không phụ thuộc Open Session in View. Mục tiêu `spring.jpa.open-in-view=false` khi triển khai API; cấu hình hiện tại chưa đặt giá trị này.
- Enum nghiệp vụ ở module sở hữu; `OrderStatus` không đặt vào common. DTO tiền dùng BigDecimal; timestamp dùng Instant.

## Thêm module mới

Khi cần thêm module mới, nhóm cần thống nhất rõ owner, liệt kê dữ liệu/contract/phụ thuộc, cập nhật allowlist nếu thay đổi thiết kế. Chỉ tạo tầng cần dùng và thêm README cho thư mục mới; không tạo class rỗng như một tính năng hoàn tất.
