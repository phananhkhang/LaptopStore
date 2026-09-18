# Ownership và phối hợp nhóm

## Phân công đề xuất cho bốn người

A/B/C/D là vị trí phân công, **chưa phải tên thành viên**. Leader điền tên/GitHub và điều chỉnh khối lượng trước khi nhận task. Owner chịu trách nhiệm API, service, dữ liệu, migration, test và tài liệu của module; không chia cố định một người chỉ controller, một người chỉ repository.

| Vị trí | Module/phần chính | Reviewer phối hợp | Kết quả bàn giao |
|---|---|---|---|
| A | auth, user, security, config, exception, common; điều phối docs/build | C; đổi auth/security thêm D | Đăng nhập/role/ownership, chuẩn lỗi, môi trường chung |
| B | catalog, inventory, procurement | C cho tồn; A cho migration | Catalog hoạt động, nhập hàng tăng tồn một lần, stock contract ổn định |
| C | cart, order | B cho tồn; D cho thanh toán/coupon | Giỏ → checkout → giao/hủy; rollback và idempotency |
| D | payment, promotion, review | C cho contract đơn; A cho callback bảo mật | COD/mô phỏng, coupon cạnh tranh, đánh giá sau mua |

Người B ưu tiên catalog trước, procurement sau; người D ưu tiên payment COD/coupon trước review. Người A không phải người duy nhất viết test hay review toàn bộ code. Cân lại nhiệm vụ mỗi mốc nếu một người bị nghẽn.

| Vị trí | Họ tên | GitHub username | Người thay thế |
|---|---|---|---|
| A | Chưa gán | Chưa gán | Chưa gán |
| B | Chưa gán | Chưa gán | Chưa gán |
| C | Chưa gán | Chưa gán | Chưa gán |
| D | Chưa gán | Chưa gán | Chưa gán |

## Quyền sở hữu dữ liệu

| Module | Dữ liệu sở hữu | Quy tắc ghi |
|---|---|---|
| auth | refresh_tokens | Chỉ auth ghi |
| user | users, roles, user_roles, addresses | Chỉ user ghi; auth/security qua service |
| catalog | products, variants, images, specifications, brands, categories | Catalog ghi metadata; biến động tồn do inventory điều phối qua catalog stock contract |
| cart | carts, cart_items | Cart ghi; order gọi cart service trong transaction checkout |
| order | orders, items, history | Order ghi; payment chỉ thông báo qua order service |
| payment | payments, payment_transactions | Chỉ payment ghi |
| promotion | coupons, coupon_usages | Promotion ghi; order gọi reserve/release |
| inventory | stock_movements | Append only; đi cùng cập nhật tồn |
| procurement | suppliers, purchase_orders, purchase_order_items | Chỉ procurement ghi |
| review | reviews | Chỉ review ghi |

## Giao việc qua contract

Ví dụ C cần trừ tồn: C mô tả `deductForOrder(orderId, lines, operationKey)`; B xác nhận DTO, lỗi, tính idempotent và transaction. C viết order gọi contract đó, B viết inventory/catalog stock và test. C không sửa ProductVariantRepository để nhanh hoàn thành.

PR đổi API/dữ liệu chung cần owner cung cấp và owner sử dụng review. Khi bất đồng, nhóm cần họp thống nhất phương án với người quyết định; không âm thầm đổi quy tắc trong README con.

## Tự động hóa ownership

Sau khi có username GitHub thật của các thành viên, leader có thể cấu hình file CODEOWNERS tại **root repository** `.github/CODEOWNERS` và thiết lập review phù hợp. Vì repo root là `LaptopStore`, các đường dẫn bắt đầu bằng `/backend/`. Đặt ở `backend/.github/` sẽ không tạo cơ chế ownership cho cả repository.
