# Module review — Đánh giá sau mua

**Owner đề xuất:** D theo [bảng ownership](../../../../../../../docs/architecture/03-ownership.md). Chưa gán thành viên thật. **Trạng thái:** skeleton hướng dẫn; các class dưới đây chưa triển khai. Thiết kế chi tiết cần được nhóm chốt khi nhận task.

## Trách nhiệm

Đánh giá một dòng hàng đã giao và kiểm duyệt nội dung. Không thay đổi đơn hoặc sản phẩm.

## Cần viết gì

| Thành phần | Class định hướng |
|---|---|
| Entity | Review |
| Repository | ReviewRepository |
| Service | ReviewService |
| Controller | ReviewController, StaffReviewController |
| Request | CreateReviewRequest, UpdateReviewRequest |
| Response/contract | ReviewResponse |

Đây là danh sách gợi ý theo use case, không bắt buộc tạo đủ class trước khi có nhu cầu.

## Dữ liệu và phụ thuộc

**Dữ liệu sở hữu:** reviews.

**Được gọi:** order.service để xác nhận dòng hàng thuộc actor và DELIVERED; catalog.service để lấy dữ liệu public khi cần. Không gọi repository/entity module khác. Xem [allowlist và contract](../../../../../../../docs/architecture/04-dependencies.md).

## Bất biến và ràng buộc

- Một review trên order_item_id; người đánh giá phải là chủ đơn đã DELIVERED.
- Rating 1..5 được kiểm tra ở request, service và CHECK DB.
- userId, productId/variantId xác định từ principal và order item; không tin trường client để chứng minh đã mua.
- USER chỉ sửa review của mình. STAFF/ADMIN được đổi cờ `visible`; ERD không có cột lưu lý do/người ẩn, nên muốn audit moderation phải mở rộng ERD trước. Không đổi rating thay khách.
- Chỉ review hiển thị được dùng tính điểm trung bình. Không tạo quan hệ service ngược catalog → review; frontend gọi API review riêng.

## API hiện có

| Endpoint | Quyền | Ghi chú |
|---|---|---|
| `POST /api/v1/me/reviews` | USER | Body `{orderItemId, rating 1..5, comment ≤ 2000}` |
| `PUT /api/v1/me/reviews/{id}` | USER (chủ review) | Không sửa được review đã bị ẩn |
| `GET /api/v1/me/reviews` | Đăng nhập | Phân trang; gồm cả review đã ẩn |
| `GET /api/v1/reviews/{id}` | Public | 404 nếu review đã ẩn |
| `GET /api/v1/products/{productId}/reviews` | Public | Phân trang, chỉ review hiển thị, mặc định mới nhất |
| `GET /api/v1/products/{productId}/reviews/summary` | Public | `averageRating`, `totalReviews`, `ratingCounts` (đủ 1..5) |
| `GET /api/v1/staff/reviews?visible=&rating=` | STAFF/ADMIN | Danh sách duyệt, lọc tùy chọn |
| `PATCH /api/v1/staff/reviews/{id}/visibility` | STAFF/ADMIN | Body `{visible}` |

Mã lỗi: 400 `VALIDATION_ERROR`, 403 `REVIEW_FORBIDDEN`, 404 `RESOURCE_NOT_FOUND`, 409 `REVIEW_ALREADY_EXISTS` (kể cả 2 request đồng thời), 422 `REVIEW_NOT_ALLOWED` (đơn chưa giao, sửa review đã ẩn).

## Lấy product/user qua order (không thêm cột vào `reviews`)

Nhóm chốt giữ ERD: `reviews` chỉ có `order_item_id`. Product, variant, chủ review lấy qua `OrderQueryService`:

- `getOrderItemForReview(id)`: kiểm tra chủ đơn và DELIVERED khi tạo/sửa.
- `getOrderItemIdsByProduct(productId)`: review theo sản phẩm và điểm trung bình (`WHERE order_item_id IN (...)`).
- `getOrderItemIdsByUser(userId)`: review của tôi.
- `getOrderItemsForReview(ids)`: lấy thông tin cả trang trong một lần gọi để tránh N+1.

## Luồng triển khai điển hình

Kiểm tra order item qua order → xác minh actor/trạng thái → insert review → trả DTO.

## Bàn giao và kiểm tra

Các ca cần chứng minh: chưa mua; đơn chưa giao; order item người khác; đánh giá trùng; rating 0/6; sửa review người khác. Cập nhật API, migration và test module cùng PR; ghi kết quả thật. Owner phối hợp người dùng contract khi đổi input/output/lỗi.

## Quy tắc các tầng và liên kết kiến trúc

Quy tắc và ràng buộc chi tiết cho từng tầng (`controller`, `service`, `repository`, `entity`, `dto`, `mapper`) tuân thủ theo [luật tầng](../../../../../../../docs/architecture/02-structure-and-layers.md) và [luồng nghiệp vụ](../../../../../../../docs/architecture/06-business-flows.md).
