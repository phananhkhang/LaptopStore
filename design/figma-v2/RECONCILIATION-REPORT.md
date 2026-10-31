# Đối chiếu giao diện và prototype với dự án LaptopStore

Ngày rà soát: 25/10/2026. Phạm vi thay đổi: Figma Design và tài liệu trong `design/`.

[Mở Figma Design](https://www.figma.com/design/mTuhPG74JMEAuqKNrL8KSn/LaptopStore?node-id=52-9809) · [Chạy prototype](https://www.figma.com/proto/mTuhPG74JMEAuqKNrL8KSn/LaptopStore?node-id=52-9809&starting-point-node-id=52%3A9809&page-id=10%3A135)

## Kết quả

Giữ phạm vi 57 màn gốc, bổ sung 35 màn/trạng thái và 25 hộp thoại. Tổng: **92 màn/trạng thái + 25 hộp thoại**; 41 khách hàng, 28 staff, 22 admin và 1 trang bắt đầu. Trang bắt đầu có mục lục toàn bộ trạng thái, bốn điểm bắt đầu prototype. File vẫn dùng 53 component, auto layout, text tiếng Việt Be Vietnam Pro và asset vector chỉnh sửa được.

Đây là thiết kế và dữ liệu prototype minh họa. Các thao tác và giao dịch trong Figma không gọi API, không phát sinh đơn/thanh toán thật. Các trường biểu mẫu mô phỏng bằng dữ liệu mẫu chọn sẵn; Figma không thay thế biểu mẫu web nhập tự do hoặc database. Luồng kho dùng fixture của từng kịch bản, không phải sổ kho dùng chung có giao dịch đồng thời.

## Sai lệch đã sửa

| Phạm vi | Sai lệch tìm thấy | Kết quả sau sửa |
| --- | --- | --- |
| Điều hướng và vai trò | Bộ lọc đơn khách mở nhận hàng staff; nút quay lại staff mở đơn khách; chọn SKU nhập hàng mở admin catalog | Định tuyến theo từng vai trò/ngữ cảnh. SKU picker chỉ đọc. Admin giữ sidebar admin khi vào nghiệp vụ chung. |
| Catalog | Product card mở cùng ROG; chọn 32GB không đổi SKU/giá | Dữ liệu từng sản phẩm, lựa chọn biến thể, ảnh vector phù hợp; cấu hình 16GB/32GB cập nhật SKU, giá và giỏ. |
| Giỏ và checkout | Không đổi số lượng/tổng; chọn VNPay nhảy trước khi đặt đơn | Giới hạn theo tồn mẫu, cập nhật tổng/coupon; chọn phương thức tách khỏi đặt đơn; snapshot đơn độc lập sản phẩm đang xem. |
| VNPay | Thiếu chờ xác minh, lỗi trả về và đối soát | Có chờ kết quả máy chủ, lỗi tải, mã đơn không hợp lệ, thành công/thất bại/hết hạn và REQUIRES_RECONCILIATION. Không kết luận PAID chỉ vì redirect. PAID hợp lệ chuyển đơn sang CONFIRMED theo code mới. |
| Hủy và trả hàng | Thiếu xác nhận, điều kiện trạng thái, bảo vệ khoản tiền đã nhận | USER hủy khi PENDING_PAYMENT/WAITING_CONFIRMATION; giao thất bại cần xác nhận hàng về; PAID/cần đối soát chặn hủy/trả và mở thông báo xử lý tiền trước. Không giả lập API hoàn tiền chưa có. |
| Staff xử lý đơn | Dùng PROCESSING thay PACKED; thiếu giao thất bại/giao lại/RETURNED | Dùng enum runtime; đủ xác nhận → đóng gói → giao → đã giao; nhánh thất bại, tối đa 3 lần, hủy và trả hàng hợp lệ. |
| COD và giao dịch | Dòng online mở COD; thiếu nhiều lần thử thanh toán | Tách online chỉ đọc và COD. COD chỉ khi DELIVERED + PENDING, xác nhận lặp không thu lần hai; có lịch sử attempt và trạng thái cần đối soát. |
| Nhập hàng | DRAFT/RECEIVED mở cùng phiếu nhận một phần; hủy sai phiếu | Phân biệt nháp, nhận một phần, nhận đủ, hủy nháp; giữ trạng thái khi mở lại; nhận phần còn lại, chặn nhận lặp; thêm nhiều SKU và nhận dòng Dell mẫu. Nhãn số lượng xác nhận theo phiếu. |
| Sản phẩm quản trị | Thêm mới dùng form sửa; thiếu variantName/status, specs/media | Tách tạo/sửa; quản lý nhiều SKU, tên biến thể, cấu hình, giá và trạng thái; thông số và thư viện ảnh. Tồn chỉ đọc trong catalog, thay đổi qua nghiệp vụ kho. |
| Danh mục/thương hiệu | Tạo/sửa/lưu thiếu phản hồi | Form riêng, đổi trạng thái, cập nhật dòng đã chọn, tạo dòng mới. |
| Tài khoản | Chi tiết không khớp dòng; nhiều vai trò/khóa thiếu tương tác | Giữ định danh từng người; chọn nhiều vai trò; không cho bỏ hết; trạng thái và vai trò còn đúng khi mở lại; nhân sự mới có ID mẫu riêng. |
| Nhà cung cấp/coupon | Tạo và sửa trộn; lưu không hiện bản ghi | Đúng dữ liệu dòng đã chọn; tạo mới xuất hiện danh sách; coupon mới không có lịch sử dùng giả. |
| Đánh giá/tài khoản/AI | Đọc review mở form; đơn chưa giao mở viết; thiếu hội thoại mới | Tách review công khai và viết/sửa sau DELIVERED, đúng Dell đã mua; chọn/đổi địa chỉ; hội thoại mới và các gợi ý AI theo nhu cầu mẫu. |
| Lọc/sắp xếp | Nút không phản hồi hoặc dẫn sai màn | Sắp xếp tăng/giảm/mới nhất; bộ lọc tác động đúng bảng, có kết quả rỗng; phân trang chỉ một trang được vô hiệu hóa rõ. |
| Chất lượng prototype | Figma lưu không đủ chuỗi nhiều nhánh if/else-if | Chuyển thành các điều kiện tuần tự có chốt để chỉ một nhánh chạy; đọc lại reaction thực tế và kiểm tra kết quả. |

## Căn cứ source

Ưu tiên enum, DTO, controller và service runtime khi tài liệu cũ khác code.

- `backend/docs/architecture/12-ERD-LaptopStore.md`: 25 bảng, variants giữ tồn, user có nhiều role, review theo order item, payment có nhiều transaction.
- `backend/docs/architecture/13-roles-and-functions.md`, `06-business-flows.md`: phạm vi ba vai trò và luồng nghiệp vụ.
- `backend/src/main/java/org/akira/laptopstore/order/entity/enums/OrderStatus.java`, `order/statemachine/OrderStateMachine.java`, `order/service/impl/OrderServiceImpl.java`: trạng thái, hủy, giao lại, trả, hết hạn.
- `order/service/impl/OrderPaymentServiceImpl.java`: xác nhận online đã kiểm chứng chuyển PENDING_PAYMENT → CONFIRMED.
- `payment/service/Impl/PaymentImpl.java`: phương thức thanh toán đã chọn, COD sau DELIVERED, hạn 15 phút, chặn retry PAID/CANCELLED/REQUIRES_RECONCILIATION.
- `payment/service/Impl/OrderPaymentPolicyImpl.java`: khoản tiền PAID/cần đối soát phải xử lý trước hủy/trả.
- `payment/service/VnpayCallbackService.java`, `PaymentTransactionServiceImpl.java`, enums payment: xác minh callback, late success, nhiều attempt, REQUIRES_RECONCILIATION.
- `frontend/src/modules/payment/pages/VnpayReturnPage.tsx`: đọc kết quả phía server, chờ/poll, lỗi tải, mã đơn không hợp lệ.
- DTO/controller/service catalog, inventory/procurement, user/address, promotion, review và router frontend đã được đối chiếu theo từng phần trong lần rà soát.

Các file order/payment/frontend đang có thay đổi trong workspace cũng được đọc lại ở cuối để cập nhật thiết kế. Không sửa hoặc ghi đè các thay đổi ứng dụng đó.

## Phần thiết kế đã có nhưng code cần hoàn thiện

Các mục này là chênh lệch triển khai, không phải chức năng backend đã được xác nhận chạy nhờ Figma:

- Đổi mật khẩu, ghi thông số/hình ảnh, tạo STAFF trực tiếp.
- Sửa phiếu nhập nháp; dashboard/báo cáo tổng hợp và xuất dữ liệu thực.
- API staff xem chi tiết đơn và lịch sử transaction cần kiểm tra/hoàn thiện quyền truy cập; các API owner hiện tại không tự trở thành staff API.
- Tạo product: request bắt SKU/price nhưng service tạo product chưa tự tạo variant đầu tiên; thiết kế có bước tạo cấu hình để thể hiện công việc cần làm.
- Promotion controller có quyền STAFF ghi coupon trong khi ma trận đặc tả baseline giới hạn ADMIN. Thiết kế theo baseline ADMIN, cần thống nhất hợp đồng.
- Chưa có luồng API hoàn tiền/giải quyết đối soát hoàn chỉnh; thiết kế chặn thao tác và hướng dẫn xử lý, không có nút giả “đã hoàn tiền”.

Nhiều route/dashboard frontend hiện còn placeholder hoặc chưa mount. Figma bao phủ đặc tả; không đồng nghĩa toàn bộ giao diện ứng dụng đã triển khai.

## Kiểm chứng sau sửa

Đọc lại từ Figma thật:

| Kiểm tra | Kết quả |
| --- | --- |
| Root màn/trạng thái/hộp thoại | 117 |
| Node / text node | 16.884 / 5.170 |
| Component tái sử dụng | 53 |
| Reaction / cạnh chuyển màn kể cả nhánh điều kiện | 1.718 / 1.873 |
| Root thiếu, đích lỗi, root không tới được từ start | 0 / 0 / 0 |
| Missing font, tràn khung theo kiểm tra auto layout, control đang bật thiếu reaction | 0 / 0 / 0 |
| Kịch bản đánh giá logic từ reaction và variable đã lưu | **22/22 đạt** |

22 kịch bản gồm SKU/giá/quantity/coupon, chặn quá tồn, đăng nhập quay về mua ngay, snapshot COD, đặt VNPay và chờ xác minh, hủy, chặn online chưa trả tiền, 3 lần giao thất bại, COD sau DELIVERED và chống thu lặp, nhận một phần/mở lại/nhận đủ/chống lặp, hủy đúng phiếu, đa vai trò/mở lại, bản ghi mới, đối soát, trả hàng đã thu tiền, nhiều SKU và lọc đúng bảng.

Đã kiểm tra ảnh render sau sửa của checkout VNPay, quản lý biến thể, nhận hàng, chờ xác minh VNPay và thanh toán cần đối soát; các màn gốc và một số modal đã được kiểm tra trong các lượt trước. Đây là kiểm tra cấu trúc, ảnh render và mô phỏng độc lập các action đã lưu; không ghi nhận là đã thao tác toàn bộ trong chế độ Present của trình duyệt.

`reconciliation-audit.json` giữ kết quả và từng kịch bản. `state.json`/`reconcile-state.json` giữ node ID thật, URL mới và kết quả cuối. Các file `reconcile-*.js` là nguồn từng giai đoạn có phụ thuộc thứ tự/context, không chạy lại hàng loạt vì các giai đoạn tạo node không idempotent.
