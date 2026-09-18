# LaptopStore backend

Backend Java Spring Boot, tổ chức theo nghiệp vụ và phân tầng Controller → Service → Repository. Bộ khung tài liệu ngày 2026-09-18 giúp owner biết phần mình được viết, phụ thuộc nào được phép và tiêu chí bàn giao.

## Bắt đầu

1. Đọc [kiến trúc và hiện trạng](docs/architecture/01-overview.md).
2. Xem [ownership](docs/architecture/03-ownership.md), điền tên/GitHub của bốn thành viên.
3. Đọc README tại module được giao và README của tầng sắp sửa.
4. Tra [phụ thuộc và contract](docs/architecture/04-dependencies.md) trước khi gọi module khác.
5. Làm theo [quy trình đóng góp](CONTRIBUTING.md) và [tiêu chí hoàn tất](docs/architecture/10-delivery-checklist.md).

## Thư mục chính

| Vị trí | Nội dung |
|---|---|
| `src/main/java/org/akira/laptopstore/` | 10 module nghiệp vụ và 4 package hạ tầng dùng chung |
| `src/main/resources/` | Cấu hình và migration |
| `src/test/` | Test theo module và test tích hợp |
| `docs/architecture/` | Thiết kế, ranh giới, luồng, quy tắc và ownership (10 tài liệu chuẩn hóa) |

## Trạng thái thực tế

Source nhận vào chỉ có `LaptopStoreApplication` và một test `contextLoads`. Chưa có API nghiệp vụ, entity hay SQL migration. Các thư mục mới được giữ trong Git bằng README; tên class trong hướng dẫn là việc cần triển khai, không phải class đã tồn tại.

`pom.xml` hiện ghi Java **25**, Spring Boot **4.1.1**; Docker dùng Temurin 25 và PostgreSQL 17. Không đổi phiên bản trong lần bổ sung tài liệu này. Security, JWT, Bean Validation, OpenAPI và Testcontainers còn cần bổ sung dependency/cấu hình khi triển khai. Xem [hiện trạng và khoảng trống](docs/architecture/01-overview.md).

## Chạy và kiểm tra

Từ thư mục `backend`, tạo `.env` từ `.env.example`, điền cấu hình cá nhân rồi dùng `docker compose up -d --build`. Chi tiết chạy Maven/DB và các giới hạn hiện tại ở [hướng dẫn vận hành](docs/architecture/09-testing-and-operation.md). Chưa có `/actuator/health`, Swagger hoặc API sản phẩm để dùng làm smoke test.
