# Tài nguyên kiểm thử

Owner module giữ fixture tối thiểu tái lập; A giữ cấu hình test dùng chung. Không copy .env thật. Chưa có profile test/DB tự động; thêm khi triển khai test với PostgreSQL. Không đưa dữ liệu demo vào production migration.

[db/verify_init_schema.sql](db/verify_init_schema.sql) kiểm schema V1 trên PostgreSQL test riêng: metadata, role seed, quan hệ và FK/UQ/CHECK. Chạy với psql -v ON_ERROR_STOP=1 sau Flyway migrate; fixture ROLLBACK dữ liệu nhưng sequence có thể tăng. Không chạy trên database dự án đang dùng.

Đọc [kiến trúc](../../../docs/architecture/README.md) và [kiểm thử/vận hành](../../../docs/architecture/09-testing-and-operation.md).
