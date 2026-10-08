# Test module cart

**Owner:** C. Đọc [quy tắc module](../../../../../../main/java/org/akira/laptopstore/cart/README.md). Đã có test controller/service và test tích hợp PostgreSQL opt-in tại `cart/integration/CartPersistenceTest.java`.

## Các test hiện có

- `CartServiceImplTest`: ID dòng giỏ, giá và tổng tiền, gộp variant, flush ID, quantity sai/tràn số, ownership, xóa/dọn giỏ, snapshot bất biến và xóa chọn lọc.
- `CartControllerTest`: JSON trả cartItemId, actor từ principal, xác thực và endpoint clear.
- `CartArchitectureTest`: cart không phụ thuộc catalog repository/entity.
- `CartPersistenceTest`: CRUD thật, orphan removal, hai request thêm đồng thời, checkout chặn ghi giỏ, rollback cleanup và MANDATORY. Test chỉ nạp entity/service/repository cart.

Chạy unit/web/architecture từ backend với JDK 25:

```powershell
mvn '-Dtest=CartServiceImplTest,CartControllerTest,CartArchitectureTest' test
```

Test tích hợp mặc định bị skip. Tạo **database PostgreSQL riêng cho test**, không dùng DB phát triển; Flyway áp dụng migration và fixture test được ghi vào DB này:

```powershell
mvn '-Dcart.test.jdbc-url=jdbc:postgresql://127.0.0.1:55437/cart_test' '-Dcart.test.username=cart_test' '-Dtest=CartPersistenceTest' test
```

Thêm `-Dcart.test.password=...` nếu DB test yêu cầu mật khẩu. Không commit credential. Khi target còn migration đã xóa khỏi source, dọn build output trước khi chạy để tránh Flyway thấy migration cũ.

Kết quả rà soát 2026-10-04: JDK 25, Spring Boot 4.1.1, PostgreSQL 18.3 trên database tạm riêng; 34 ca pass, gồm 5 integration, 24 service/query, 3 web và 2 architecture (rule cart của bộ test chung và CartArchitectureTest). Test cart không chứng minh toàn ứng dụng khởi động được: cấu hình nạp tất cả module hiện gặp lỗi entity Coupon yêu cầu cột status nhưng schema V1 chưa có; lỗi này ngoài phạm vi cart/order và không được sửa trong thay đổi này.

## Hành vi cần chứng minh

quantity 0/âm; hai lần thêm cùng SKU không tạo dòng trùng; không sửa giỏ người khác; checkout đồng thời cập nhật giỏ.

Unit test cho service/quy tắc; web test cho DTO/status/role; repository test dùng PostgreSQL cho query/constraint/lock. Luồng nhiều module đặt ở integration và phối hợp owner contract. Không viết test để mirror getter hay công bố concurrency bằng test đơn luồng.

Tên class `*Test`/`*Tests` phù hợp runner hiện tại; `*IT` chỉ dùng khi cấu hình thêm Failsafe. Mỗi test dữ liệu độc lập, không phụ thuộc thứ tự hoặc DB cá nhân. Ghi lệnh/kết quả thật trong PR.
