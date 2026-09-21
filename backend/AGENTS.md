# Quy tắc làm việc trong backend

1. Đọc `docs/architecture/README.md` và README module trước khi sửa code.
2. Không gọi repository/entity của module khác; giao tiếp qua service/DTO contract.
3. Quan hệ JPA chỉ trong cùng module; cross-module chỉ giữ ID dù DB có FK.
4. Catalog không được ghi tồn. Inventory sở hữu `inventory_stocks` và `stock_movements`.
5. Controller không gọi repository trực tiếp; transaction đặt ở service/use case.
6. Không sửa migration Flyway đã chạy/merge; schema mới luôn có migration mới.
7. Không commit `.env`, secret, `target/`, `.idea/`.
8. Trước PR chạy test phù hợp và cập nhật tài liệu khi đổi API/schema/contract.
9. Không thêm microservice, broker hoặc abstraction lớn nếu chưa có use case rõ ràng.
10. Nếu thay đổi dependency module, cập nhật `04-dependencies.md` và ArchUnit rule trong cùng PR.
