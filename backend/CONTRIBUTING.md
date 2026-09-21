# Contributing

## Trước khi code

- Pull/rebase nhánh mới nhất.
- Xác định owner module và contract chéo cần dùng.
- Nếu đổi DB: kiểm tra version Flyway cuối trên `main` rồi dùng version kế tiếp.

## Trong khi code

- Controller → Service → Repository trong cùng module.
- Cross-module chỉ gọi service/DTO contract; không import entity/repository module khác.
- Entity không serialize trực tiếp ra API.
- Migration phải có FK/UNIQUE/CHECK/index cần thiết; không dựa `ddl-auto=update`.
- Use case tiền/tồn/coupon cần xem xét idempotency và transaction.

## Trước Pull Request

```bash
./mvnw test
```

Nếu test cần PostgreSQL, chạy DB/test environment theo `docs/architecture/09-testing-and-operation.md`.

PR phải ghi: thay đổi gì, module owner, migration mới, API/contract thay đổi, test đã chạy và giới hạn còn lại. Checklist chi tiết ở `docs/architecture/10-delivery-checklist.md`.
