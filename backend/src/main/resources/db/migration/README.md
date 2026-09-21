# Migration Flyway

## Quy ước

Dùng timeline phẳng trong `db/migration`, version tăng toàn cục:

```text
V1__create_identity_schema.sql
V2__create_catalog_schema.sql
V3__create_cart_schema.sql
V4__create_inventory_schema.sql
V5__create_promotion_schema.sql
V6__create_order_schema.sql
V7__create_payment_schema.sql
V8__create_procurement_schema.sql
V9__create_review_schema.sql
```

Giai đoạn bootstrap có thể dùng **một migration khởi tạo cho mỗi module/nhóm nghiệp vụ**. Sau khi file đã merge hoặc chạy ở DB dùng chung, **không sửa migration cũ**; mọi thay đổi tạo file mới, ví dụ:

```text
V10__add_warranty_months_to_products.sql
V11__add_order_status_index.sql
```

## Luật nhóm

- Trước khi tạo migration mới, pull/rebase `main` rồi lấy version kế tiếp.
- Bảng cha tạo trước foreign key phụ thuộc.
- Constraint quan trọng phải nằm trong DB: FK, UNIQUE, CHECK, index.
- Không dùng `ddl-auto=update` thay Flyway; cấu hình giữ `validate`.
- Demo/seed data tách khỏi migration production hoặc có profile/strategy rõ.
- Test cả DB trống và upgrade từ schema trước đó trước merge.
- Không chia subfolder theo module nếu chưa cấu hình Flyway locations riêng; timeline phẳng dễ review dependency hơn.

Đọc [Entity model](../../../../../docs/architecture/11-entity-model.md) và [Dữ liệu/nhất quán](../../../../../docs/architecture/05-data-and-consistency.md) trước khi viết SQL.
