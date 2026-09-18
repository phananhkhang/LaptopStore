# Kiểm tra ranh giới kiến trúc

Owner A. Hiện chưa có ArchUnit/dependency hoặc test enforce tự động. Khi thêm, kiểm controller không gọi repository; module chỉ dùng service/DTO allowlist; common không import module; graph không cycle; stock contract chỉ inventory gọi. Test cần phân biệt import DTO/entity và hướng phụ thuộc thực, không chỉ tên folder.

Đọc [kiến trúc](../../../../../../../docs/architecture/README.md) và [kiểm thử/vận hành](../../../../../../../docs/architecture/09-testing-and-operation.md).
