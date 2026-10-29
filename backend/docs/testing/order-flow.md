# Kiểm thử Cart / Checkout / Order

## Môi trường và kết quả

Đã chạy trên Windows, JDK 25, PostgreSQL 18.3 riêng ở localhost và Vite/Edge headless. Flyway áp dụng V1–V7 thành công trên DB trống. Không sử dụng DB phát triển.

- Full Maven verify: 185 tests tổng, 184 đạt, 1 test contextLoads bị @Disabled từ trước.
- Sau khi bổ sung retry thanh toán và race callback/expire: OrderFlowPersistenceTest 17/17 đạt, CartUnavailableItemTest 1/1 đạt trên DB trống mới.
- Frontend build TypeScript/Vite đạt. 22/22 kịch bản Playwright đạt, chạy cả desktop 1440×1000 và mobile 390×844; ảnh và trace nằm trong artifact commerce-ui-results của CI.

## Coverage có giá trị

| Nhóm | Đã xác minh |
| --- | --- |
| Checkout | Chốt giá/tổng/note/method ở server; stock/ledger/coupon/cart cùng transaction |
| Concurrency | Hai request cùng key trả cùng order sau khi cart đã trống; hai khách tranh một SKU không bán vượt tồn |
| Idempotency | Key riêng theo user; đổi method/note cùng key bị chặn; cancel/expire/collect COD lặp không ghi lại side effect |
| Rollback | Trigger DB gây lỗi ở cleanup cart sau khi đã ghi order/stock/coupon; lỗi ghi restore khi cancel/expire; toàn bộ thay đổi rollback và retry được |
| Lifecycle | Không nhảy cóc; giao thất bại tối đa 3 lần; giao lại không trừ stock; hủy hoàn stock/coupon một lần |
| Payment | IPN ký đúng mới xác nhận; sai chữ ký/amount không cập nhật; lặp/FAILED sau SUCCESS không lùi trạng thái; retry không tạo order mới; late success giữ chứng cứ để đối soát |
| Race | Callback cùng lúc expire có một kết quả nhất quán: CONFIRMED/PAID hoặc CANCELLED/REQUIRES_RECONCILIATION |
| API/UI | Ownership, STAFF/ROLE_STAFF, server filter/page, ảnh local, không tràn ngang, coupon/quantity, confirm/cancel, retry lỗi mạng/API, browser query không giả PAID |

## Chạy lại

Tạo PostgreSQL disposable với user có quyền tạo schema/trigger. Các test thêm fixture có UUID; OrderFlowPersistenceTest dọn các đơn do từng test tạo. CI đã cấu hình PostgreSQL service riêng và bật cả cart/order persistence tests.

PowerShell, trong backend (đặt ORDER_TEST_JDBC_URL, ORDER_TEST_USER, ORDER_TEST_PASSWORD cho DB riêng):

```powershell
.\mvnw.cmd -B verify "-Dspring.datasource.url=$env:ORDER_TEST_JDBC_URL" "-Dspring.datasource.username=$env:ORDER_TEST_USER" "-Dspring.datasource.password=$env:ORDER_TEST_PASSWORD" "-Dspring.config.import=" "-Dcart.test.jdbc-url=$env:ORDER_TEST_JDBC_URL" "-Dcart.test.username=$env:ORDER_TEST_USER" "-Dcart.test.password=$env:ORDER_TEST_PASSWORD" "-Dorder.test.jdbc-url=$env:ORDER_TEST_JDBC_URL" "-Dorder.test.username=$env:ORDER_TEST_USER" "-Dorder.test.password=$env:ORDER_TEST_PASSWORD"
```

Frontend:

```sh
npm ci
npm run build
npx playwright install --with-deps chromium # Linux/CI; Windows dùng Edge đã cài
npm run test:e2e -- --workers=2
```

Playwright dùng API stub để kiểm tra tương tác/contract và kết quả render. Các bất biến tiền/tồn/rollback/concurrency được chứng minh bằng service proxy thật trên PostgreSQL. IPN ký và xử lý nội bộ; chưa giao dịch với VNPay sandbox thật vì cần merchant credentials hợp lệ.

## Fix từ regression

- Cart vẫn trả cartItemId của biến thể bị ẩn/xóa để khách xóa được; sellable=false và availableQuantity=0 chặn checkout.
- Thu COD khóa delivered COD order, khởi tạo payment còn thiếu rồi ghi PAID trong cùng transaction; request lặp không tạo payment thứ hai.
- Thời hạn VNPay chuẩn hóa giây để response lần đầu và lần retry khớp độ chính xác lưu DB/provider.
- Lỗi xóa giỏ được hiển thị sau khi đóng dialog; preview checkout có nút Kiểm tra lại khi API lỗi.

## Smoke-test qua API thật

Đã chạy frontend qua dev:commerce → backend Spring Boot → PostgreSQL disposable (không mock): đăng nhập, Catalog → Cart (quantity 2), coupon SMOKESAVE500 → Checkout → tạo đơn COD → tải lại phiên → Staff xác nhận/đóng gói/giao/thu COD. Đối chiếu DB: DELIVERED, COD/PAID, tổng 49.500.000 VND, ghi chú được giữ, một SALE movement cho đơn.

Regression bổ sung: apiClient và AuthProvider dùng chung refreshAccessToken single-flight để React StrictMode không dùng lại refresh cookie đã rotate. Catalog public query dùng path tương đối với baseURL; dev:commerce proxy đúng backend 8081 hoặc API_PROXY_TARGET.
