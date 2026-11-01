# Dữ liệu demo (chỉ môi trường dev)

`R__seed_demo_catalog.sql` thêm 30 laptop mẫu (thương hiệu, danh mục, biến thể, thông số và ảnh trong `backend/uploads/products`). Thư mục này **không** nằm trong `db/migration` nên production không chạy.

Bật trong `backend/.env`:

    FLYWAY_LOCATIONS=classpath:db/migration,classpath:db/seed

Script idempotent:

- Chỉ thêm brand/category/product chưa có (so khớp theo tên), biến thể chưa có SKU.
- Tồn kho ban đầu của biến thể mới được ghi kèm `stock_movements` (`reference_type = DEMO_SEED`).
- Chỉ gắn ảnh/thông số cho sản phẩm chưa có.
- Đổi `status = 'AVAILABLE'` (dữ liệu mẫu cũ) thành `ACTIVE` để giỏ hàng/checkout bán được.

Flyway chạy lại file khi nội dung đổi. Tắt seed bằng cách bỏ `classpath:db/seed`; `spring.flyway.ignore-migration-patterns` đã bỏ qua repeatable migration bị thiếu.
