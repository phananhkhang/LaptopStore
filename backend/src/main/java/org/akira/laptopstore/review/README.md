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
- USER chỉ sửa review của mình. STAFF/ADMIN được ẩn nội dung kèm lý do; không đổi rating thay khách.
- Chỉ review hiển thị được dùng tính điểm trung bình. Không tạo quan hệ service ngược catalog → review; frontend gọi API review riêng.

## Luồng triển khai điển hình

Kiểm tra order item qua order → xác minh actor/trạng thái → insert review → trả DTO.

## Bàn giao và kiểm tra

Các ca cần chứng minh: chưa mua; đơn chưa giao; order item người khác; đánh giá trùng; rating 0/6; sửa review người khác. Cập nhật API, migration và test module cùng PR; ghi kết quả thật. Owner phối hợp người dùng contract khi đổi input/output/lỗi.

## Quy tắc các tầng và liên kết kiến trúc

Quy tắc và ràng buộc chi tiết cho từng tầng (`controller`, `service`, `repository`, `entity`, `dto`, `mapper`) tuân thủ theo [luật tầng](../../../../../../../docs/architecture/02-structure-and-layers.md) và [luồng nghiệp vụ](../../../../../../../docs/architecture/06-business-flows.md).

