# Cart, Checkout và Order

Giao diện dựa trên Figma LaptopStore Orange Commerce, page 10:135: Cart 10:206, Checkout 10:207, đặt hàng 10:208, đơn của tôi 10:798, chi tiết 10:799, Staff list/detail 10:209 và 10:210. Màu #c74615, font Be Vietnam Pro và SVG local trong public/commerce. Ảnh sản phẩm lấy động từ Catalog khi có productId.

Routes: /cart, /checkout, /orders, /orders/:orderId, /orders/:orderId/success, /payment/vnpay-return, /staff/orders, /staff/orders/:orderId. Các màn sử dụng API thật; Playwright stub API chỉ trong tests. Role STAFF và ROLE_STAFF đều được chuẩn hóa khi kiểm route. Backend vẫn quyết định quyền.

Cart dùng cartItemId cho PATCH/DELETE, nhận sellable/availableQuantity từ server, preview lại coupon khi sửa giỏ. Nút thêm từ Catalog gọi API Cart. Checkout preview có chống phản hồi cũ; giữ Idempotency-Key theo user/payload trong sessionStorage khi request không rõ kết quả. Đơn đã commit nhưng Payment lỗi đi tới chi tiết để tiếp tục, tránh tạo đơn lại.

Order list hỗ trợ search/status/page trên server; summary có items/paymentMethod. Staff detail trả allowedTransitions và UI chỉ hiện các bước đó. Hủy, giao thất bại/trả hàng và thu COD có dialog xác nhận. Kết quả VNPay chỉ tin GET payment từ backend, không tin query trả về browser; pending được kiểm tra có giới hạn rồi cho kiểm tra thủ công.

Các màn có loading, empty, lỗi/retry và responsive. Số liệu Staff ghi rõ là trong trang hiện tại. Widget AI không đè các màn checkout/order. Những module Catalog/Account/Inventory ngoài luồng mua và xử lý đơn giữ thiết kế hiện có.

## Kiểm thử UI

Chạy backend Spring Boot ở cổng mặc định 8081, rồi `npm run dev:commerce` để proxy `/api` và `/uploads` đúng backend. Có thể đặt `API_PROXY_TARGET` khi dùng Docker/cổng khác. Lệnh này kế thừa config Vite chung và không cần thay đổi config đang dùng cho các module khác. Catalog product API dùng path `/products/...`, vì `/api/v1` đã nằm trong apiClient.baseURL.

npm run test:e2e -- --workers=2 chạy Playwright desktop/mobile; Windows dùng Edge, Linux dùng Chromium đã cài bằng npx playwright install --with-deps chromium. API stub chỉ dùng trong test. CI lưu screenshot và trace vào commerce-ui-results. Bộ kiểm thử DB và giới hạn xác minh VNPay ghi trong backend/docs/testing/order-flow.md.

Phiên đăng nhập dùng refreshAccessToken single-flight chung cho bootstrap và interceptor 401 để không rotate cùng cookie hai lần khi StrictMode mount. Smoke-test API thật đã qua login/reload → Catalog → Cart → Checkout → Staff → COD; bằng chứng và kết quả trong backend/docs/testing/order-flow.md.
