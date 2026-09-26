# Ownership và phối hợp nhóm

## Phân công đề xuất cho bốn người

A/B/C/D là vị trí phân công, **chưa phải tên thành viên**. Leader điền tên/GitHub và điều chỉnh khối lượng trước khi nhận task. Owner chịu trách nhiệm API, service, dữ liệu, migration, test và tài liệu của module; không chia cố định một người chỉ controller, một người chỉ repository.

| Vị trí | Module/phần chính | Reviewer phối hợp | Kết quả bàn giao |
|---|---|---|---|
| A | auth, user, security, config, exception, common; điều phối docs/build | C; đổi auth/security thêm D | Đăng nhập/role/ownership, chuẩn lỗi, môi trường chung |
| B | catalog, inventory, procurement | C cho checkout/tồn; A cho migration | Catalog hoạt động, tồn và ledger nhất quán, nhập hàng tăng tồn một lần |
| C | cart, order | B cho tồn; D cho thanh toán/coupon | Giỏ → checkout → giao/hủy; rollback và idempotency |
| D | payment, promotion, review | C cho contract đơn; A cho callback bảo mật | COD/mô phỏng, coupon cạnh tranh, đánh giá sau mua |

Người B ưu tiên catalog + inventory trước, procurement sau; người D ưu tiên payment COD/coupon trước review. Người A không phải người duy nhất viết test hay review toàn bộ code. Cân lại nhiệm vụ mỗi mốc nếu một người bị nghẽn.

| Vị trí | Họ tên | GitHub username | Người thay thế |
|---|---|---|---|
| A | Chưa gán | Chưa gán | Chưa gán |
| B | Chưa gán | Chưa gán | Chưa gán |
| C | Chưa gán | Chưa gán | Chưa gán |
| D | Chưa gán | Chưa gán | Chưa gán |

## Quyền sở hữu dữ liệu

Nguyên tắc: quyền ghi phải bám sát ERD và đi qua một use case owner rõ ràng. Theo ERD, tồn hiện tại nằm ở cột `product_variants.quantity`; mọi thay đổi cột này phải đi qua inventory service và ghi `stock_movements` trong cùng transaction.

| Module | Dữ liệu sở hữu | Quy tắc ghi |
|---|---|---|
| auth | refresh_tokens | Chỉ auth ghi |
| user | users, roles, user_roles, addresses | Chỉ user ghi; auth/security qua service |
| catalog | products, product_variants, product_images, product_specifications, brands, categories | Catalog ghi metadata, SKU, giá và thông số; không tự thay đổi `product_variants.quantity` |
| cart | carts, cart_items | Cart ghi; order gọi cart service trong transaction checkout |
| order | orders, order_items, order_status_history | Order ghi; payment chỉ thông báo qua order service |
| payment | payments, payment_transactions | Chỉ payment ghi |
| promotion | coupons, coupon_usages | Promotion ghi; order gọi reserve/release |
| inventory | `product_variants.quantity` (quyền ghi nghiệp vụ), stock_movements | Inventory là nơi duy nhất tăng/giảm quantity và phải ghi ledger cùng transaction |
| procurement | suppliers, purchase_orders, purchase_order_items | Chỉ procurement ghi; nhận hàng gọi inventory |
| review | reviews | Chỉ review ghi |

Foreign key ở DB có thể tham chiếu bảng module khác, nhưng Java không dùng repository/entity module khác. `stock_movements.variant_id` tham chiếu `product_variants.id`; inventory dùng contract của catalog để khóa/cập nhật quantity thay vì import repository catalog trực tiếp.

## Giao việc qua contract

Ví dụ C cần trừ tồn: C mô tả `deductForOrder(orderId, lines, operationKey)`; B xác nhận DTO, lỗi, idempotency và transaction. C viết order gọi contract đó, B viết logic khóa/cập nhật `product_variants.quantity`, tạo `StockMovement` và test. C không sửa repository catalog trực tiếp để nhanh hoàn thành.

PR đổi API/dữ liệu chung cần owner cung cấp và owner sử dụng review. Khi bất đồng, nhóm cần họp thống nhất phương án với người quyết định; không âm thầm đổi quy tắc trong README con.

## Tự động hóa ownership

Sau khi có username GitHub thật của các thành viên, leader có thể cấu hình file CODEOWNERS tại **root repository** `.github/CODEOWNERS` và thiết lập review phù hợp. Vì repo root là `LaptopStore`, các đường dẫn bắt đầu bằng `/backend/`. Đặt ở `backend/.github/` sẽ không tạo cơ chế ownership cho cả repository.
