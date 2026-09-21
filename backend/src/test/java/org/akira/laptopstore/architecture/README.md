# Architecture tests

Owner A điều phối, nhưng mỗi owner phải sửa rule khi thay đổi dependency hợp lệ. `ModuleArchitectureTest` dùng ArchUnit để tự động chặn các lỗi kiến trúc cơ bản:

- controller phụ thuộc trực tiếp repository;
- module import repository/entity của module khác;
- module gọi business module ngoài allowlist trong `04-dependencies.md`;
- vòng phụ thuộc giữa top-level package/module.

README/allowlist vẫn là nguồn mô tả đầy đủ; ArchUnit là lớp bảo vệ tự động, không thay review thiết kế.
