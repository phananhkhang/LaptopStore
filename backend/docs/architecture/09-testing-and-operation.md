# Kiểm thử và vận hành

## Công cụ hiện có và giới hạn

POM có starter test cho JPA/Flyway/Web MVC và ArchUnit. Source có `contextLoads` cùng `ModuleArchitectureTest` để enforce các ranh giới nền; chưa có Testcontainers, OpenAPI, Actuator hay CI riêng.

## Chạy bằng Docker Compose

Từ `backend`, sao chép `.env.example` thành `.env`, điền cấu hình cá nhân. Windows PowerShell: `Copy-Item .env.example .env`; chỉ chạy nếu chưa có `.env`, không ghi đè cấu hình đang dùng.

```bash
docker compose up -d --build
docker compose ps
docker compose logs --tail=100 app
```

Compose hiện dùng service `app`, `postgres`, host app port mặc định 8080, DB host port mặc định 5432. Nếu đụng cổng, đổi APP_PORT/DB_HOST_PORT. Trong container app, DB host mặc định `postgres:5432`; không dùng host port cho giao tiếp nội bộ.

Chỉ DB có healthcheck. `/`, `/api/v1/products`, `/actuator/health`, Swagger chưa được triển khai; 404 ở các path này chưa chứng minh app chết. Kiểm tra process/log kết nối DB trong giai đoạn bootstrap. Sau khi có feature, bổ sung smoke test endpoint thật. Dockerfile hiện skip test khi build image, vì vậy image build thành công không có nghĩa test đã pass.

## Chạy Maven trên máy phát triển

Yêu cầu JDK 25 theo pom. Linux/macOS dùng `./mvnw`, Windows PowerShell dùng `.\mvnw.cmd`. Wrapper tải Maven/dependency khi cần; cần kết nối repository phù hợp.

Spring Boot chạy trực tiếp không tự đọc `.env` của Compose. Cần đặt DB_HOST=localhost, DB_PORT đúng cổng DB host, DB_NAME/DB_USERNAME/DB_PASSWORD trong môi trường tiến trình hoặc cấu hình IDE. Có thể chạy riêng DB bằng `docker compose up -d postgres`.

```bash
./mvnw test
./mvnw verify
./mvnw spring-boot:run
```

`contextLoads` cần DB và migration phù hợp vì ứng dụng có datasource. Chưa có profile test tự provision PostgreSQL. Không ghi kết quả “pass” nếu thiếu DB. `verify` hiện chưa có cấu hình Maven Failsafe riêng; test tên `*IT` không tự được đảm bảo chạy. Trước khi thêm Failsafe, dùng `*Tests`/`*Test` cho test muốn Surefire chạy hoặc cấu hình plugin rõ trong task.

## Chiến lược test theo rủi ro

| Mức | Đối tượng | Ca có giá trị |
|---|---|---|
| Unit | Service/quy tắc | Tổng tiền, coupon, transition, ownership với stub/query contract |
| Web | Controller/security | Validation, status/body, role, principal, error filter |
| Repository | SQL/JPA trên PostgreSQL | Constraint, query phân trang, lock, version, FK |
| Integration | Use case nhiều module | Checkout rollback, oversell, hủy lặp, nhập lặp, callback lặp/trễ |
| Architecture | Import/đồ thị | ArchUnit: controller không phụ thuộc repository; module không import repository/entity module khác; kiểm cycle khi có đủ class |

Test khóa/constraint dùng PostgreSQL 17 thật hoặc Testcontainers khi thêm dependency, không coi H2 là bằng chứng tương đương. Test concurrency cần hai transaction/thread độc lập với dữ liệu kiểm soát; test chạy một transaction không chứng minh chống oversell. Ưu tiên test hành vi, không viết test chỉ lặp lại getter/mapper tầm thường.

## Logging và vận hành dữ liệu

Log request/trace id, order/payment/purchase reference đủ truy vết. Business conflict bình thường không in stack trace ồn ào; unexpected error có trace id, không trả chi tiết cho client. Không log password/token/raw callback secret.

`show-sql=true` đang bật trong application.properties; khi triển khai môi trường dùng chung/prod cần cấu hình log phù hợp. Hiện chưa có profile prod file dù Compose có biến SPRING_PROFILES_ACTIVE. Database password fallback hiện có trong cấu hình gốc không được xem là cấu hình production.

DB cần backup và phương án khôi phục trước migration phá dữ liệu. Chạy migration trên DB trống và bản schema trước đó; giữ volume khi rebuild app. Không dùng `docker compose down -v` trong quy trình cập nhật thông thường.
