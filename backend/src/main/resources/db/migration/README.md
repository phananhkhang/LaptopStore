# Migration Flyway

## V1 khởi tạo toàn bộ schema

`V1__init_schema.sql` tạo schema cho **database mới/trống** theo [Entity model](../../../../../docs/architecture/11-entity-model.md) và [ERD LaptopStore](../../../../../docs/architecture/12-ERD-LaptopStore.md):

- 25 bảng, 192 cột, 30 FK và 14 UQ ngoài PK.
- 24 PK `BIGSERIAL`; bảng `user_roles` có PK ghép `(user_id, role_id)`.
- Seed ba role `ROLE_USER`, `ROLE_STAFF`, `ROLE_ADMIN`; không seed tài khoản hoặc dữ liệu kinh doanh.
- Bảng cha được tạo trước bảng con; index cho FK/query, không tạo lại index đã có qua PK/UQ.
- Không có `inventory_stocks`, `audit_logs` hoặc bảng reservation ngoài ERD.

V1 mới thay thế file khởi tạo identity cũ theo yêu cầu gộp bootstrap. Repository chỉ giữ **một file version 1**. Không dùng `IF NOT EXISTS` để che một schema cũ/sai cấu trúc; Flyway quản lý việc chạy một lần.

## Database đã chạy V1 identity cũ

V1 cũ là `V1__create_identity_schema.sql`, chỉ tạo 5 bảng identity. Nếu database đã ghi V1 đó vào `flyway_schema_history`, V1 mới sẽ không tự tạo thêm bảng và Flyway validation sẽ báo checksum khác. Khi rà soát ngày 2026-10-01, database dự án đang có bản ghi V1 identity thành công.

- Dùng V1 mới với database trống riêng.
- Muốn giữ dữ liệu/database đã chạy V1 cũ: giữ lại đúng V1 đã áp dụng và viết V2 nâng cấp 20 bảng còn lại trong một thay đổi riêng.
- Không dùng `flyway repair`, xóa lịch sử hoặc reset database để thay thế migration nâng cấp.

Lần thay đổi này chỉ tạo artifact V1 và kiểm thử database riêng; không áp dụng V1 mới hoặc thay lịch sử của database dự án.

## Quyết định triển khai trong V1

Ảnh ERD chỉ mô tả tên cột, PK/FK/UQ. Các chi tiết dưới đây là lựa chọn triển khai, không phải ràng buộc đọc từ ảnh:

| Nhóm | Lựa chọn |
|---|---|
| ID, số lượng, cờ | ID/FK nội bộ BIGINT; quantity/delta/rating/limit INTEGER; active/default/primary/visible BOOLEAN. |
| Tiền và discount_value | NUMERIC(19,2), dùng BigDecimal; giữ hai chữ số thập phân của source hiện có, không dùng float/double. |
| Thời gian | TIMESTAMPTZ cho field Instant; TIMESTAMP cho LocalDateTime trong catalog/inventory/procurement để tương thích entity hiện tại. Thống nhất Instant/UTC là việc tiếp theo. |
| Mã ngoài | provider_transaction_id BIGINT theo field Long hiện có; reference_id VARCHAR(255) theo field String. Contract provider cần được chốt trước khi đổi kiểu. |
| Trạng thái | VARCHAR, không thêm CHECK tập trạng thái chưa được use case thống nhất; orders mặc định PENDING theo enum hiện tại. |
| Actor/khởi tạo history | changed_by_user_id có thể null cho hệ thống; from_status có thể null khi ghi trạng thái đầu. |
| Xóa | CASCADE cho bảng nối, địa chỉ/token/cart và con thuộc aggregate catalog/order/procurement. Các FK tới lịch sử order/payment/review/movement hoặc actor dùng NO ACTION mặc định để bảo vệ tham chiếu. Không hard-delete dữ liệu lịch sử qua use case. |
| Timestamp mặc định | CURRENT_TIMESTAMP khi INSERT; ứng dụng/JPA chịu trách nhiệm cập nhật updated_at, không có trigger cập nhật tự động. |

CHECK trong V1 bảo vệ: token version không âm; giá/tiền/tồn không âm; quantity giỏ/đơn/phiếu > 0; received_quantity nằm trong 0..quantity; total_amount đúng công thức; ledger after = before + delta; giới hạn coupon không âm và thời gian bắt đầu trước kết thúc nếu có cả hai; rating 1..5.

Giữ đúng UQ của ERD: không thêm UQ `product_specifications.product_id`, cặp cart/variant, cặp purchase-order/variant hoặc provider transaction. Hai khai báo UQ khác trong JPA hiện vẫn được ghi ở tài liệu 11; Hibernate validate không thay thế việc kiểm tra những ràng buộc đó.

## Kiểm thử

Trên database PostgreSQL kiểm thử riêng, chạy `LaptopStoreApplicationTests` để Flyway migrate và Hibernate `ddl-auto=validate` toàn bộ entity. Sau đó dùng fixture [verify_init_schema.sql](../../../../test/resources/db/verify_init_schema.sql) với `psql -v ON_ERROR_STOP=1` để kiểm số bảng/cột/PK/FK/UQ, role seed, quan hệ và các ca dữ liệu không hợp lệ. Fixture dùng transaction rồi ROLLBACK, chỉ chạy trên database test.

## Migration tiếp theo

Timeline phẳng trong `db/migration`, version tăng toàn cục: `V2__...sql`, `V3__...sql`, ... Sau khi migration đã merge/chạy ở DB dùng chung, giữ nguyên nội dung; schema thay đổi bằng version mới. Yêu cầu gộp V1 này chỉ áp dụng cho bootstrap database mới.

Trước PR kiểm database trống và đường upgrade nếu có; không dùng `ddl-auto=update`. Đọc thêm [Dữ liệu/nhất quán](../../../../../docs/architecture/05-data-and-consistency.md).
