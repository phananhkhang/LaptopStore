# LaptopStore — Figma Design đã rà soát

[Mở Figma Design](https://www.figma.com/design/mTuhPG74JMEAuqKNrL8KSn/LaptopStore?node-id=52-9809) · [Chạy prototype](https://www.figma.com/proto/mTuhPG74JMEAuqKNrL8KSn/LaptopStore?node-id=52-9809&starting-point-node-id=52%3A9809&page-id=10%3A135)

Page **LaptopStore · UI & Prototype / Đã rà soát dự án** nằm đầu file. Giữ phạm vi 57 màn chính; tổng sau bổ sung là **92 màn/trạng thái và 25 hộp thoại**: 41 khách hàng, 28 staff, 22 admin, một trang bắt đầu.

Thiết kế có layer chữ Be Vietnam Pro, auto layout, semantic variables, 53 component và assets vector chỉnh sửa được. Mục lục mở được toàn bộ màn và hộp thoại; bốn điểm bắt đầu cho chọn vai trò, mua sắm, staff và admin.

Đã rà với ERD, tài liệu, DTO/service/controller và code frontend hiện tại, kể cả thay đổi payment mới trong workspace. Đã sửa điều hướng sai vai trò, lựa chọn SKU/giá, giỏ/checkout, VNPay/COD/đối soát, hủy/trả/giao lại, nhập hàng nhiều đợt, CRUD quản trị, phân quyền và bộ lọc.

**Kiểm chứng:** 22/22 kịch bản logic từ reaction Figma đã lưu đạt; 117 root đều tới được từ trang bắt đầu; không phát hiện font thiếu, tràn khung, đích lỗi hoặc control đang bật thiếu reaction. Có 1.718 reaction, 1.873 cạnh chuyển màn tính cả nhánh điều kiện. Đã kiểm tra ảnh render các màn chính và trạng thái bổ sung. Chưa xác nhận thao tác toàn bộ trong chế độ Present.

Xem [bản đối chiếu và giới hạn triển khai](RECONCILIATION-REPORT.md) cùng [kết quả kiểm chứng](reconciliation-audit.json). Các API chưa có được ghi riêng; prototype dùng dữ liệu mẫu, không kết nối backend hay thanh toán thật.

`state.json`, `reconcile-state.json` lưu ID cuối và URL start mới `52:9809`. Các file JavaScript là nguồn từng giai đoạn dùng qua Figma Plugin API trong phiên thiết kế, có phụ thuộc context; không chạy lại toàn bộ. Bản HTML `../preview.html` được giữ riêng.
