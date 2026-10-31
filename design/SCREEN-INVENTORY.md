# LaptopStore — 53 màn hình và trạng thái thiết kế

Đối chiếu source ngày **24/10/2026**. Bộ thiết kế gồm **1 màn chọn vai trò + 22 màn khách hàng/auth + 18 màn vận hành + 12 màn quản trị = 53 frame desktop**. Một số frame là trạng thái trước/sau của cùng use case, không phải 53 URL ứng dụng. Không thay đổi phần triển khai ứng dụng trong lần làm thiết kế này.

**Ký hiệu:** **FE** = đã có component/route frontend; **FE mẫu** = mock, placeholder hoặc mới có một phần giao diện; **BE** = có API/service liên quan nhưng chưa có giao diện tương ứng; **Đặc tả** = thiết kế theo tài liệu/ERD, chưa có endpoint hoàn chỉnh. Các nhãn này xác nhận việc có source, không xác nhận kiểm thử hệ thống đang chạy.

## Khách hàng, xác thực và điều hướng — 23 frame

Các URL có sẵn đối chiếu với [AppRouter](../frontend/src/app/router/AppRouter.tsx) và [CatalogRoutes](../frontend/src/modules/catalog/routes/CatalogRoutes.tsx). Route bắt đầu `/api/v1` là API, không phải URL màn hình.

| # | Key / màn hình | Route hoặc use case | Dữ liệu ERD / trạng thái hiện tại |
|---|---|---|---|
| 01 | `start` — Chọn vai trò | Điểm vào prototype | `users`, `roles`, `user_roles`; **Đặc tả** điều hướng thiết kế, chưa có màn chọn vai trò trong app. |
| 02 | `home` — Trang chủ mua hàng | `/` hiện chuyển sang `/products` | `products`, `brands`, `categories`, `product_variants`, `product_images`; **FE mẫu**, trang chủ riêng là thiết kế bổ sung. |
| 03 | `catalog` — Danh sách, tìm kiếm và bộ lọc | `/products`; GET `/api/v1/products/search` | Catalog như trên; **FE mẫu** đang dùng 2 sản phẩm mock, bộ lọc placeholder; **BE** có keyword/name-or-SKU, brand/category, RAM/storage, min/max giá và sort. |
| 04 | `detail` — Chi tiết laptop | `/products/:id`; GET `/products/{id}` và images/specification | `products`, `product_variants`, `product_images`, `product_specifications`; **FE mẫu** đang dùng mock, nút chọn variant/thêm giỏ chưa nối use case tại trang này. |
| 05 | `cart` — Giỏ hàng có sản phẩm | `/cart`; `/api/v1/me/cart` | `carts`, `cart_items`, `product_variants`; **FE**, có load/thêm/sửa/xóa qua API giỏ hàng. Coupon trên bản thiết kế được xác thực ở checkout. |
| 06 | `emptyCart` — Giỏ hàng trống | Trạng thái `/cart` | Cùng dữ liệu giỏ hàng; **FE** đã xử lý trạng thái trống, minh họa được thiết kế mới. |
| 07 | `checkout` — Địa chỉ, phương thức, coupon và đặt đơn | `/checkout`; POST `/api/v1/me/orders/preview`, `/me/orders` | `addresses`, `orders`, `order_items`, `coupon_usages`, `product_variants`, `stock_movements`; **FE** có form/API preview và tạo đơn. |
| 08 | `success` — Đặt hàng thành công / COD | Trạng thái sau POST order | `orders`, `order_items`, `payments`; **BE** trả đơn; frontend hiện chuyển thẳng `/orders/:orderId`, chưa có trang thành công riêng. |
| 09 | `payPending` — Tiếp tục VNPay | POST `/api/v1/payments`, `/{paymentId}/vnpay-session` | `payments`, `payment_transactions`; **BE** có tạo payment/session, chưa có frontend thanh toán. Callback chưa hoàn tất. |
| 10 | `payFail` — Thanh toán thất bại / thử lại | Trạng thái payment; chưa có route frontend | `payments`, `payment_transactions`; **BE một phần**: có status/session và lịch sử, chưa có luồng callback hoàn chỉnh hay giao diện retry. |
| 11 | `orders` — Đơn hàng của tôi | `/orders`; GET `/api/v1/me/orders` | `orders`, `order_items`; **FE** có danh sách API, bộ lọc/thiết kế card được mở rộng trong prototype. |
| 12 | `tracking` — Chi tiết và timeline đơn | `/orders/:orderId`; GET `/api/v1/me/orders/{orderId}` | `orders`, `order_items`, `order_status_history`, `payments`; **FE** có chi tiết/timeline cơ bản; thanh toán được đọc riêng, không phải trường trên order. |
| 13 | `review` — Viết/sửa đánh giá đã mua | POST/PUT `/api/v1/reviews`; chưa có route frontend | `reviews` → `order_items` → `orders`; **BE** kiểm chủ đơn, `DELIVERED`, rating 1–5, một review/dòng hàng. |
| 14 | `profile` — Hồ sơ | `/profile`; GET/PATCH `/api/v1/me` | `users`, `roles`, `user_roles`; **FE** cập nhật họ tên/điện thoại, email chỉ đọc. |
| 15 | `addresses` — Sổ địa chỉ | `/me/addresses`; `/api/v1/me/addresses` | `addresses`; **FE** tab hồ sơ có list/create/delete; đường dẫn này vẫn render `ProfilePage`, chưa tự chọn tab địa chỉ. |
| 16 | `addressEdit` — Thêm/sửa địa chỉ | Form địa chỉ; POST/PATCH `/api/v1/me/addresses/{id}` | `addresses`: recipientName/phone/street/ward/district/city/postalCode/isDefault; **FE** có thêm; **BE** có sửa, giao diện sửa bổ sung. |
| 17 | `password` — Đổi mật khẩu | Chưa có route/endpoint | `users.password_hash`; **Đặc tả** từ nhóm tài khoản, chưa có change-password use case trong controller. |
| 18 | `login` — Đăng nhập | `/login`; POST `/api/v1/auth/login` | `users`, `roles`, `user_roles`, `refresh_tokens`; **FE** có xác thực và quay lại trang trước. |
| 19 | `register` — Đăng ký | `/register`; POST `/api/v1/auth/register` | Cùng identity; **FE** họ tên/email/phone/password/confirmPassword. |
| 20 | `ai` — Tư vấn AI và chat | `/ai-assistant`; POST `/api/v1/ai/chat` | Đọc `products`, `product_variants`; **FE** có full chat và widget toàn app. Context có SKU/RAM/storage/color/price/quantity; chưa có bảng/lịch sử chat lưu trên server. |
| 21 | `emptySearch` — Không có kết quả | Trạng thái tìm/lọc catalog | Catalog; **Đặc tả UI** bổ sung cho bộ lọc frontend còn mẫu. |
| 22 | `forbidden` — 403 | `/403` | Role/ownership; **FE** đã có trang từ chối truy cập. |
| 23 | `notFound` — 404 | Catch-all | **FE** đã có component 404; cần kiểm lại ưu tiên wildcard catalog/admin khi tích hợp. |

Nguồn chi tiết: [frontend customer modules](../frontend/src/modules), [OrderController](../backend/src/main/java/org/akira/laptopstore/order/controller/OrderController.java), [PaymentController](../backend/src/main/java/org/akira/laptopstore/payment/controller/PaymentController.java), [ReviewServiceImpl](../backend/src/main/java/org/akira/laptopstore/review/service/impl/ReviewServiceImpl.java), [AIServiceImpl](../backend/src/main/java/org/akira/laptopstore/ai/service/impl/AIServiceImpl.java).

## Nhân viên và nghiệp vụ dùng chung — 18 frame

STAFF vận hành đơn hàng, tồn kho, nhập hàng, thanh toán và kiểm duyệt đánh giá. ADMIN có thể thực hiện nghiệp vụ vận hành. Thiết kế giữ điều chỉnh kho, quản lý role và sửa catalog trong khu quản trị theo [phân quyền đặc tả](../backend/docs/architecture/13-roles-and-functions.md).

| # | Key / màn hình | Route hoặc API liên quan | Dữ liệu ERD / trạng thái hiện tại |
|---|---|---|---|
| 24 | `staffOverview` — Tổng quan vận hành | `/staff/*` hiện là dashboard placeholder | `orders`, `payments`, `product_variants`, `purchase_orders`; **FE mẫu**; KPI/tổng hợp là **Đặc tả**, chưa có endpoint dashboard. |
| 25 | `staff` — Danh sách đơn | `/staff/orders`; GET `/api/v1/staff/orders` | `orders`, `order_items`, `users`; **FE** danh sách, bộ lọc/KPI mở rộng trong thiết kế. |
| 26 | `staffDetail` — Chi tiết đơn cần xử lý | Chưa có route/API staff detail riêng | `orders`, `order_items`, `order_status_history`, `payments`; **Đặc tả** view vận hành; GET own-order không thay thế API staff xem đơn khách khác. |
| 27 | `staffConfirmed` — Kết quả xác nhận đơn | Trạng thái sau xác nhận | Cùng dữ liệu đơn; **Đặc tả**, OrderController/OrderService hiện chưa có handler chuyển trạng thái staff. |
| 28 | `inventory` — Tồn kho và cảnh báo | GET `/api/v1/staff/inventory/low-stock` | `product_variants.quantity`; **BE** có low-stock. **FE source** `InventoryDashboardPage` nhưng `InventoryRoutes` chưa được mount trong AppRouter. |
| 29 | `stockHistory` — Ledger kho | GET `/api/v1/staff/inventory/movements`, `/variants/{id}/history` | `stock_movements`, `product_variants`, `users`; **BE** có lịch sử; **FE source** history chưa được mount. |
| 30 | `purchaseOrders` — Danh sách phiếu nhập | GET `/api/v1/staff/purchase-orders` | `purchase_orders`, `purchase_order_items`, `suppliers`; **BE**, chưa có frontend. |
| 31 | `purchaseEdit` — Tạo phiếu nhập | POST `/api/v1/staff/purchase-orders` | Cùng procurement + `product_variants`; **BE** có tạo DRAFT; chưa có endpoint sửa phiếu trong controller hiện tại. |
| 32 | `purchaseReceive` — Nhận một phần/nhận đủ | POST `/api/v1/staff/purchase-orders/{id}/receive` | `received_quantity`, `product_variants.quantity`, `stock_movements`; **BE** có service nhận hàng, chưa có frontend. |
| 33 | `purchaseDone` — Nhận hàng thành công | Trạng thái RECEIVED sau receive | Cùng dữ liệu nhập hàng; **BE** trả trạng thái; màn kết quả mới. |
| 34 | `staffSuppliers` — Tra cứu nhà cung cấp | GET `/api/v1/staff/suppliers`, `/{id}` | `suppliers`, `purchase_orders`; **BE** có đọc; staff không có nút sửa trong thiết kế. |
| 35 | `payments` — Giám sát thanh toán | Chưa có API danh sách staff tương ứng | `payments`, `payment_transactions`, `orders`; **Đặc tả** dashboard/list staff; API transaction hiện chỉ cho USER và kiểm ownership. |
| 36 | `paymentDetail` — Thanh toán COD chi tiết | PUT `/api/v1/payments/cod` | `payments`, `orders`, `payment_transactions`; **BE** có ghi nhận COD; view detail staff và dữ liệu tổng hợp chưa có endpoint hoàn chỉnh. |
| 37 | `codConfirmed` — COD đã thu | Kết quả recordCodPayment | Cùng payment; **BE** có use case, chưa có frontend. Không suy ra đã thu tiền chỉ từ DELIVERED. |
| 38 | `moderation` — Đánh giá đang hiển thị | GET `/api/v1/reviews`; PUT `/api/v1/staff/reviews/{id}/visibility` | `reviews`, `order_items`, `orders`; **BE** có danh sách visible và toggle; chưa có staff list kèm author/product enrichment. |
| 39 | `moderationHidden` — Đã ẩn/hiện lại đánh giá | Cùng visibility endpoint | `reviews.visible`; **BE** có mutation, chưa có endpoint danh sách toàn bộ/đã ẩn và frontend moderation. |
| 40 | `suppliers` — Quản lý nhà cung cấp | POST/PUT/PATCH `/api/v1/admin/suppliers` | `suppliers`; **BE** CRUD/status thực, chưa có frontend; màn nằm trong workspace ADMIN. |
| 41 | `supplierEdit` — Sửa nhà cung cấp/lịch sử nhập | Admin supplier + staff purchase-order read | `suppliers`, `purchase_orders`, `purchase_order_items`; **BE** có sửa/đọc, tổng hợp lịch sử theo supplier là phần thiết kế; workspace ADMIN. |

Nguồn: [InventoryController](../backend/src/main/java/org/akira/laptopstore/inventory/controller/InventoryController.java), [InventoryRoutes](../frontend/src/modules/inventory/routes/InventoryRoutes.tsx), [PurchaseOrderController](../backend/src/main/java/org/akira/laptopstore/procurement/controller/PurchaseOrderController.java), [PurchaseOrderServiceImpl](../backend/src/main/java/org/akira/laptopstore/procurement/service/impl/PurchaseOrderServiceImpl.java), [SupplierServiceImpl](../backend/src/main/java/org/akira/laptopstore/procurement/service/impl/SupplierServiceImpl.java), [StaffReviewController](../backend/src/main/java/org/akira/laptopstore/review/controller/StaffReviewController.java).

## Quản trị dữ liệu và kinh doanh — 12 frame

| # | Key / màn hình | Route hoặc API liên quan | Dữ liệu ERD / trạng thái hiện tại |
|---|---|---|---|
| 42 | `admin` — Tổng quan kinh doanh | `/admin/*` hiện dashboard placeholder | Orders/payments/catalog/inventory; **FE mẫu**, KPI/doanh thu/bestseller là **Đặc tả**, chưa có report endpoint. |
| 43 | `products` — Quản lý sản phẩm | `/admin/products` khai báo trong CatalogRoutes; `/api/v1/admin/products` | `products`, `brands`, `categories`, `product_variants`; **FE một phần** list fetch, nút thêm còn alert. Route admin wildcard hiện có thể đưa vào dashboard thay vì catalog child. |
| 44 | `productEdit` — Thông tin/variant/ảnh/spec | POST/PUT/PATCH admin product và variants | `products`, `product_variants`, `product_images`, `product_specifications`; **BE** có ghi product/variant/status; chưa có API ghi ảnh/spec tương ứng thiết kế. Form editor mới. |
| 45 | `categories` — Danh mục và form chỉnh sửa | `/admin/categories` trong CatalogRoutes; `/api/v1/admin/categories` | `categories`; **FE source** + **BE**, cần xử lý mount/ưu tiên `/admin/*` để trang tới đúng component. |
| 46 | `brands` — Thương hiệu và form chỉnh sửa | `/api/v1/admin/brands` | `brands`; **BE** CRUD/status, chưa có frontend tương ứng. |
| 47 | `users` — Khách hàng và nhân sự | GET `/api/v1/admin/users` | `users`, `roles`, `user_roles`; **BE** có list; frontend admin dashboard chưa có trang quản lý user. |
| 48 | `userDetail` — Chi tiết, khóa/mở, nhiều role | PATCH `/api/v1/admin/users/{id}/status`, `/{id}/roles` | Identity như trên; **BE** có status/roles; chưa có GET admin user-detail riêng và UI. Thông tin phải lấy từ list hoặc bổ sung endpoint phù hợp. |
| 49 | `stockAdjust` — Điều chỉnh kho có lý do | POST `/api/v1/admin/inventory/variants/{id}/adjust` | `product_variants.quantity`, `stock_movements.note`; **BE** có delta/reason/actor, chưa có form frontend. |
| 50 | `couponList` — Danh sách coupon | GET `/api/v1/coupons` | `coupons`; **BE** có list, chưa có frontend. Thiết kế chỉ ADMIN quản lý theo baseline đặc tả. |
| 51 | `couponEdit` — Điều kiện mã giảm giá | POST/PUT `/api/v1/coupons` | `coupons`: code/type/value/minOrder/maxDiscount/time/limits; **BE** có create/update, chưa có UI. Controller hiện còn cho STAFF mutation, khác ma trận quyền thiết kế. |
| 52 | `couponUsage` — Lịch sử sử dụng | GET `/api/v1/coupon-usages/coupon/{couponId}` | `coupon_usages`, `coupons`, `orders`, `users`; **BE** có đọc/release, chưa có frontend; tên khách/mã đơn trên thiết kế cần enrichment. |
| 53 | `report` — Báo cáo kinh doanh | Chưa có endpoint báo cáo | `orders`, `order_items`, `payments`, `payment_transactions`, `product_variants`; **Đặc tả** theo roles/functions. Chart/KPI là dữ liệu minh họa. |

Nguồn: [AdminProductController](../backend/src/main/java/org/akira/laptopstore/catalog/controller/AdminProductController.java), [AdminUserController](../backend/src/main/java/org/akira/laptopstore/user/controller/AdminUserController.java), [CouponController](../backend/src/main/java/org/akira/laptopstore/promotion/controller/CouponController.java), [CouponUsageController](../backend/src/main/java/org/akira/laptopstore/promotion/controller/CouponUsageController.java), [frontend admin product](../frontend/src/modules/catalog/pages/admin/AdminProductPage.tsx).

## Quy tắc dữ liệu và giới hạn tích hợp

- **Đơn hàng:** enum source hiện tại là `PENDING`, `CONFIRMED`, `PROCESSING`, `SHIPPING`, `DELIVERED`, `CANCELLED`, `RETURNED`; bản thiết kế dùng “Chờ xác nhận”, “Đã xác nhận”, “Đang chuẩn bị”, “Đang giao”, “Đã giao”, “Đã hủy”, “Đã trả hàng”. `PACKING`/`DELIVERY_FAILED` xuất hiện trong tài liệu là đề xuất, chưa có trong [OrderStatus](../backend/src/main/java/org/akira/laptopstore/order/entity/enums/OrderStatus.java). “Chờ thanh toán” là trạng thái payment hiển thị riêng. Staff xác nhận/chuẩn bị/giao và user hủy đơn vẫn thiếu endpoint mutation trong controller hiện tại.
- **Thanh toán:** `PaymentStatus` có `PENDING/PAID/FAILED/CANCELLED`, phương thức `COD/VNPAY`. Một logical payment/order; nhiều transaction/attempt. Tạo link VNPay đã có; [VNPay IPN](../backend/src/main/java/org/akira/laptopstore/payment/controller/PaymentCallbackController.java) còn TODO và chỉ trả 200. Không coi luồng online là hoàn tất đầu-cuối. Không có cột `reconciliation_status` trong ERD.
- **Procurement:** controller/interface từng có thể được xem là skeleton trong snapshot tài liệu cũ. **Source đang đối chiếu hiện đã có service thực**: create/cancel/read/list/receive, transaction/khóa phiếu, kiểm quantity mới nhận, cộng `received_quantity`, gọi inventory. Chưa có frontend, endpoint sửa phiếu và chưa chạy kiểm thử đầu-cuối trong công việc thiết kế này; không gắn nhãn skeleton cho service hiện tại.
- **Catalog:** có product/variant/brand/category mutations. API images/specification hiện là đọc; các thao tác “Thay hình ảnh”, sửa thông số là thiết kế cho triển khai tiếp. Tồn kho giữ tại `product_variants.quantity`, không tự thêm `inventory_stocks` hay `reserved_quantity`.
- **Profile/review:** review gắn duy nhất `order_item_id`, không thêm `user_id/product_id` vào `reviews`; author/product được truy qua quan hệ. ReviewResponse hiện chưa trả tác giả/sản phẩm; GET visible chưa lọc theo product. Đổi mật khẩu chỉ có yêu cầu trong tài liệu, chưa có API. `orders` không có cột note dù checkout DTO/UI có trường ghi chú; không hứa ghi chú được lưu trong snapshot đơn.
- **Tài liệu cũ:** [13 roles/functions](../backend/docs/architecture/13-roles-and-functions.md) có mục hiện trạng nói OrderController chưa có handler và payment/review/procurement chưa có API. Source hiện mới hơn những nhận xét đó; phân quyền/use case trong tài liệu vẫn dùng làm mục tiêu thiết kế, không dùng mục hiện trạng cũ để phủ nhận controller thực tế. Quyền coupon STAFF trong code cũng cần đối chiếu baseline ADMIN-only.
- **ERD:** đối chiếu [12 ERD](../backend/docs/architecture/12-ERD-LaptopStore.md), [11 entity model](../backend/docs/architecture/11-entity-model.md), [06 business flows](../backend/docs/architecture/06-business-flows.md) và [V1 schema](../backend/src/main/resources/db/migration/V1__init_schema.sql). 25 bảng: users/roles/user_roles/refresh_tokens/addresses; brands/categories/products/product_variants/product_images/product_specifications; carts/cart_items; orders/order_items/order_status_history; payments/payment_transactions; coupons/coupon_usages; suppliers/purchase_orders/purchase_order_items; stock_movements; reviews. Không có audit tổng quát; chỉ lịch sử đơn và ledger kho.

## Dữ liệu demo và prototype

Luồng mua chính dùng **ASUS ROG Strix G15**, SKU **ROG-G15-01**, 16 GB RAM/512 GB SSD/đen, đơn **LS24102601**, khách **Nguyễn Minh An**, **minhan@example.com**, địa chỉ **273 An Dương Vương, Phường 3, Quận 5, TP. Hồ Chí Minh**. Giá **25.000.000 ₫**, coupon **LAPTOP500** giảm **500.000 ₫**, vận chuyển **0 ₫**, tổng **24.500.000 ₫**. Ngày demo chính **24/10/2026**, các đơn cũ và lịch sử có ngày trước đó; thời gian giao dự kiến được ghi rõ. Mỗi frame sau xác nhận/thanh toán/nhận hàng là một trạng thái minh họa, không phải dữ liệu cập nhật trực tiếp.

Prototype liên kết màn theo vai trò và hành trình. Bấm xác nhận đơn, ghi nhận COD, nhận hàng, điều chỉnh tồn, lưu role/coupon, gửi đánh giá hoặc thanh toán chỉ chuyển trạng thái thiết kế; **không gọi API, không tạo giao dịch, không sửa dữ liệu ứng dụng**. Charts/KPI, số lượng sản phẩm/tồn kho, contact và danh sách ví dụ là dữ liệu demo, không phải báo cáo kinh doanh thật.

Mã dựng frame: [core-screens.js](figma-build/core-screens.js), [customer-secondary.js](figma-build/customer-secondary.js), [ops-secondary.js](figma-build/ops-secondary.js). Thay đổi giới hạn ở artifact trong `design/`; không triển khai/chỉnh sửa frontend/backend ứng dụng.

## Trạng thái prototype bổ sung

Ngoài 53 màn nghiệp vụ ở bảng trên, có 4 trạng thái hoàn tất luồng: staffProcessing (Đang chuẩn bị), staffShipping (Đang giao), staffDelivered (Đã giao), paySuccess (Thanh toán VNPay thành công). Tổng bàn giao: 57 màn/trạng thái. Những trạng thái này minh họa thiết kế; không xác nhận endpoint cập nhật trạng thái hoặc callback VNPay đã hoàn chỉnh.

