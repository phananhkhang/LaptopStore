# Rà soát Figma so với đặc tả, ERD và source hiện tại

Phạm vi: sửa thiết kế và prototype trong file hiện có; không đổi backend/frontend. Giữ 57 màn chính, thêm trạng thái/popup khi cần. Font/tokens/component hiện có được tái sử dụng.

## Các sai lệch đã xác nhận

1. Bộ lọc đơn khách “Tất cả” dẫn sang catalog, “Chờ xác nhận” dẫn sang nhận hàng; các trạng thái staff quay lại đơn khách. Nút thêm biến thể trong phiếu nhập dẫn sang admin catalog. Cần liên kết theo vai trò và ngữ cảnh, không suy luận bằng regex chung.
2. Chi tiết sản phẩm không đổi SKU/giá khi chọn 32GB; mọi product card mở cùng một nội dung ROG. Giỏ không đổi số lượng/tổng. Bổ sung prototype variables cho lựa chọn sản phẩm/cấu hình và giỏ.
3. Chọn VNPay nhảy thẳng sang thanh toán trước khi đặt hàng. Chọn địa chỉ không quay về checkout. Bổ sung checkout online, chọn địa chỉ và trạng thái lỗi checkout.
4. Xem review công khai mở form viết; đơn chưa giao vẫn mở form review. Đơn Dell đã giao lại mở form ROG. Tách đọc review, viết/sửa review sau DELIVERED.
5. Thiếu hủy đơn hợp lệ, hết hạn online, giao thất bại/giao lại tối đa 3 lần, RETURNED. Dùng enum và OrderStateMachine hiện tại: CREATED, PENDING_PAYMENT, WAITING_CONFIRMATION, CONFIRMED, PACKED, SHIPPING, DELIVERED, DELIVERY_FAILED, CANCELLED, RETURNED. Không gọi PACKED là PROCESSING, không thêm EXPIRED vào enum.
6. Các dòng online payment mở chi tiết COD. Thiếu payment_transactions. Xác nhận COD chỉ áp dụng COD/PENDING + DELIVERED; không có nút tự đánh dấu VNPay thành công cho staff.
7. Danh mục/thương hiệu create/edit/save đều thiếu interaction. Thêm product mở edit; thiếu variantName/status, quản lý nhiều variant, thông số 8 trường và thư viện ảnh.
8. User detail không khớp người được chọn; khóa/mở khóa và checkbox nhiều role thiếu interaction. Supplier/coupon create và edit bị trộn.
9. Phiếu DRAFT/RECEIVED đều mở nhận một phần của cùng phiếu; thiếu chi tiết nháp/hủy, selector SKU chỉ đọc và phần nhận nhiều đợt. Một số nút quay lại không hoạt động.
10. Admin vào nghiệp vụ dùng chung bị đổi sang sidebar staff; thiếu truy cập nhập hàng/thanh toán/moderation/điều chỉnh tồn từ ngữ cảnh admin.
11. Các form cần phản hồi lưu, xác nhận thao tác và lỗi validation; AI cần trạng thái hội thoại mới; các filter/sort cần phản hồi có nghĩa.

## Nguồn và khác biệt cần bàn giao

- `backend/.../order/statemachine/OrderStateMachine.java` và `OrderStatus.java`: chuẩn trạng thái runtime mới hơn docs 06/13. USER chỉ hủy PENDING_PAYMENT/WAITING_CONFIRMATION. Giao lại bị chặn sau 3 thất bại. DELIVERED có thể chuyển RETURNED.
- `PaymentImpl.recordCodPayment`: yêu cầu COD, PENDING, đơn DELIVERED. PaymentStatus PENDING/PAID/FAILED/CANCELLED/REQUIRES_RECONCILIATION; transaction PENDING/SUCCESS/FAILED/REQUIRES_RECONCILIATION (đọc lại code mới ở cuối lượt).
- `AdminProductController` và request DTO: SKU/price/variantName/ram/storage/color/status; inventory quantity không nhập trong form catalog.
- ERD: 25 bảng, variant giữ tồn, reviews liên kết order item, nhiều role, một logical payment/order có nhiều transaction.
- Những API chưa có: đổi mật khẩu, ghi specs/images, tạo STAFF trực tiếp, sửa PO nháp, dashboard/report tổng hợp, staff xem payment transactions/staff order detail riêng. Các màn này là thiết kế theo đặc tả, không phải xác nhận backend đã triển khai.
- Docs 06/13 và một số mapper/service còn lệch source hiện tại; không sửa backend trong lần rà UI. Ví dụ CreateProductRequest bắt SKU/price nhưng ProductService.createProduct chỉ lưu product, chưa tạo variant đầu tiên; promotion controller cho STAFF ghi coupon trong khi ma trận đặc tả giới hạn ADMIN.

## Kiểm chứng yêu cầu

Đọc lại canvas thật; giữ layer chỉnh sửa được; kiểm tra ảnh render các màn bổ sung; kiểm tra missing font, bounds, đích prototype, các luồng nghiệp vụ và liên kết sai vai trò. Ghi node ID thật và báo cáo sau sửa vào state/coverage.

## Kết quả cuối

Xem [bản đối chiếu](RECONCILIATION-REPORT.md). Tổng 92 màn/trạng thái + 25 hộp thoại; 22/22 kịch bản logic đạt. Bổ sung chờ xác minh trả về VNPay, lỗi tải/mã đơn không hợp lệ, cần đối soát và chặn hủy/trả khoản đã thu tiền theo code payment mới nhất trong workspace. PAID hợp lệ chuyển đơn CONFIRMED. Các chuỗi nhiều nhánh được chuyển sang điều kiện tuần tự có chốt sau khi phát hiện Figma không lưu đủ else-if.
