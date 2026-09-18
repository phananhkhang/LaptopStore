# Test tích hợp

Owner C điều phối checkout cùng B/D; A hỗ trợ DB/config. Kiểm rollback toàn bộ, oversell với hai transaction, hủy/receive lặp, coupon cuối, callback race hủy. Dùng PostgreSQL thật/Testcontainers khi thêm dependency. Không dựa H2 hoặc mock để khẳng định khóa hoạt động.

Đọc [kiến trúc](../../../../../../../docs/architecture/README.md) và [kiểm thử/vận hành](../../../../../../../docs/architecture/09-testing-and-operation.md).
