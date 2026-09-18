# Migration Flyway

Đặt SQL VyyyyMMddHHmmss__module_description.sql theo UTC, version duy nhất; thêm bảng cha trước FK. Không có file SQL mới trong bộ khung này. Không sửa migration đã chạy ở môi trường dùng chung; không dùng ddl-auto=update thay migration. Tách dữ liệu demo khỏi migration production. Kiểm DB trống và nâng cấp trước merge.

Đọc [kiến trúc](../../../../../docs/architecture/README.md) và [kiểm thử/vận hành](../../../../../docs/architecture/09-testing-and-operation.md).
