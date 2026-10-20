# Báo Cáo Nghiệm Thu Tuần 4 — Module Cart & Order (Checkout Flow)

**Người thực hiện:** Phan Anh Khang  
**Module phụ trách:** `cart`, `order` (Backend & Frontend Checkout / Order)

---

## 1. Mục tiêu Tuần 4 (Theo bảng phân công)

- **Backend:**
  - Xây dựng Checkout flow hoàn chỉnh trong `OrderService` / `OrderController`:
    - Hỗ trợ chọn địa chỉ giao hàng từ sổ địa chỉ (`addressId` qua `AddressService`) hoặc nhập trực tiếp thông tin người nhận (`recipientName`, `recipientPhone`, `shippingAddress`) và lưu snapshot vào `orders`.
    - Kiểm tra tính hợp lệ của phương thức thanh toán (`paymentMethod`: `COD`, `VNPAY`).
    - Tính toán đầy đủ `subtotal`, `discountAmount`, `shippingFee`, `totalAmount` tại server (không tin dữ liệu tiền từ client).
    - Validate tồn kho (`availableQuantity` từ `CatalogCheckoutService`) và validate điều kiện mã giảm giá (`CouponService.evaluateForCheckout`) **trước khi tạo `Order`**, sau đó ghi nhận lượt dùng mã (`CouponService.reserveForOrder`), trừ kho (`InventoryService.deductForOrder`) và xóa các mục đã mua khỏi giỏ hàng (`CartService.clearPurchasedItems`) trong cùng một `@Transactional`.
    - Mở thêm API `POST /api/v1/me/orders/preview` để xem trước kết quả tính toán Checkout (subtotal, discount, shipping, total) trước khi đặt hàng.
    - Chuẩn hóa hợp đồng `OrderQueryService` (`validateOrderOwner`, `validateOrderPayable`, `validateOrderDelivered`, `getOrderTotalAmount`) phục vụ module `payment` và `review`.
- **Frontend:**
  - Chuẩn bị và nâng cấp `CheckoutPage.tsx`, `orderApi.ts`, `order/types/index.ts`, `OrderDetailPage.tsx` hỗ trợ chọn địa chỉ đã lưu hoặc nhập mới, chọn phương thức thanh toán (`COD` / `VNPAY`), áp dụng mã giảm giá (preview), hiển thị bảng kê `subtotal / discount / shippingFee / totalAmount` và đặt hàng kèm `Idempotency-Key`.

---

## 2. Chi tiết các hạng mục đã triển khai

### 2.1. Backend (`order`, `catalog`, `promotion`)
1. **Hỗ trợ địa chỉ giao hàng (`addressId` & Snapshot):**
   - Bổ sung `addressId` và `paymentMethod` vào `CreateOrderRequest` (vẫn giữ đầy đủ constructor 6 tham số tương thích ngược với bộ test Tuần 3).
   - Khi `addressId` được gửi lên, `OrderServiceImpl` gọi `AddressService.getAddressById(userId, addressId)` để xác thực địa chỉ thuộc về chính user đang đăng nhập và tự động ghép `street, ward, district, city, postalCode` thành snapshot `shippingAddress`.
2. **Validate phương thức thanh toán (`CheckoutPaymentMethod`):**
   - Tạo enum `CheckoutPaymentMethod` (`COD`, `VNPAY`) trong module `order` (đảm bảo tuân thủ tuyệt đối rule `ModuleArchitectureTest` không phụ thuộc ngược từ `order` sang `payment`).
   - Từ chối ngay từ đầu các phương thức thanh toán không hợp lệ bằng `ValidationException`.
3. **Validate tồn kho và Coupon trước khi tạo Order:**
   - Mở rộng `CheckoutVariantSnapshot` và `CatalogCheckoutServiceImpl` trả kèm `availableQuantity` của từng biến thể dưới khóa `PESSIMISTIC_WRITE` (và hỗ trợ hàm đọc `getSellableVariants` cho preview).
   - `OrderServiceImpl` tổng hợp số lượng đặt theo từng `variantId` và kiểm tra `requestedQuantity <= availableQuantity` trước khi gọi `orderRepository.saveAndFlush(...)`.
   - Bổ sung `CouponService.evaluateForCheckout(code, userId, subtotal)` để kiểm tra trạng thái mã, thời hạn, giá trị đơn tối thiểu, giới hạn lượt dùng toàn hệ thống/mỗi user và tính `discountAmount` trước khi lưu `Order`, sau đó gọi `CouponService.reserveForOrder(...)` để lưu `CouponUsage` gắn với `orderId`.
4. **API Preview Checkout (`POST /api/v1/me/orders/preview`):**
   - Cho phép USER kiểm tra nhanh giỏ hàng, địa chỉ, mã giảm giá và nhận về `CheckoutPreviewResponse` gồm `subtotal`, `discountAmount`, `shippingFee`, `totalAmount`, `items` mà không thay đổi dữ liệu DB.
5. **Chuẩn hóa `OrderQueryServiceImpl` và cấu hình CI:**
   - Chuẩn hóa ngoại lệ `ResourceNotFoundException` và `ValidationException` trong `OrderQueryServiceImpl`.
   - Bổ sung giá trị mặc định cho cấu hình `vnpay.*` trong `VnPayGateway` và `application.properties` để đảm bảo `LaptopStoreApplicationTests` (`contextLoads`) luôn vượt qua CI.

### 2.2. Frontend (`frontend/src/modules/order`)
- Cập nhật `types/index.ts` và `api/orderApi.ts` với `previewCheckout` (`POST /api/v1/me/orders/preview`) và `createOrder` (`POST /api/v1/me/orders` kèm header `Idempotency-Key`).
- Nâng cấp `CheckoutPage.tsx` với đầy đủ 4 bước:
  1. Chọn địa chỉ từ sổ địa chỉ hoặc nhập địa chỉ mới.
  2. Chọn phương thức thanh toán (`COD` hoặc `VNPAY`).
  3. Nhập mã khuyến mãi và nhấn "Áp dụng / Tính thử" để xem số tiền được giảm.
  4. Hiển thị chi tiết từng dòng sản phẩm cùng bảng tổng hợp `Tạm tính`, `Giảm giá`, `Phí vận chuyển`, `Tổng thanh toán` và nút xác nhận đặt hàng.
- Cập nhật `OrderDetailPage.tsx` hiển thị đầy đủ `subtotal`, `discountAmount`, `shippingFee`, `totalAmount` và `couponCodeSnapshot`.

---

## 3. Kiểm thử & Nghiệm thu
- **Unit & Controller Tests:** Bổ sung đầy đủ test case trong `OrderServiceImplTest` và `OrderControllerTest` cho luồng Checkout với `addressId`, `paymentMethod`, `couponCode`, chặn vượt tồn kho/coupon hết hạn trước khi lưu Order, idempotency và preview checkout.
- **Kiến trúc (ArchUnit):** Đảm bảo 100% tuân thủ `ModuleArchitectureTest` và `CartArchitectureTest`.
- **Frontend Build:** Kiểm tra biên dịch TypeScript (`tsc -b`) thành công, không có lỗi kiểu dữ liệu.
