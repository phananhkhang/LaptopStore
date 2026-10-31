# LaptopStore — HTML hoàn thiện

**Bản Figma Design đã rà soát:** [92 màn/trạng thái + 25 hộp thoại](https://www.figma.com/design/mTuhPG74JMEAuqKNrL8KSn/LaptopStore?node-id=52-9809). Giữ phạm vi 57 màn gốc, 53 component và assets vector; 22/22 kịch bản logic prototype đạt. Xem [bản đối chiếu với dự án](figma-v2/RECONCILIATION-REPORT.md) và [chạy prototype](https://www.figma.com/proto/mTuhPG74JMEAuqKNrL8KSn/LaptopStore?node-id=52-9809&starting-point-node-id=52%3A9809&page-id=10%3A135).

Mở **preview.html** bằng Chrome, Brave hoặc Edge. File chứa sẵn font Be Vietnam Pro (400/500/600/700), CSS, JavaScript và assets SVG. Không cần Internet, cài thư viện hoặc chạy server.

## Cách xem

- File mở mặc định ở trang chủ mua hàng.
- Thanh tối trên cùng cho phép chuyển **Khách hàng / Staff / Admin**.
- **Bản đồ giao diện** liệt kê 57 màn và trạng thái.
- Biểu tượng mũi tên vòng trên thanh này khôi phục dữ liệu demo sau khi xác nhận.
- Dữ liệu thay đổi được lưu bằng localStorage trong cùng trình duyệt.

## Đã sửa

- Thay renderer tọa độ tuyệt đối bằng HTML semantic, Flexbox và CSS Grid responsive. Nội dung tự co giãn theo font và độ dài.
- Nhúng font tiếng Việt đầy đủ vào HTML để không phụ thuộc Google Fonts hoặc font hệ thống.
- Thiết kế lại trang chủ, danh sách và chi tiết sản phẩm, giỏ hàng, checkout, tài khoản, chat tư vấn và workspace Staff/Admin.
- Hoàn thiện các bộ lọc, biểu mẫu, dialog xác nhận, thông báo, trạng thái rỗng và điều hướng mobile.
- Giỏ hàng tính tiền theo SKU/số lượng; coupon kiểm điều kiện; đặt hàng trừ tồn và ghi lịch sử.
- Chuyển trạng thái đơn, chặn xác nhận online khi chưa thanh toán, thu COD sau khi đã giao; hủy đơn hoàn tồn.
- Nhập hàng nhận một phần/đủ; ghi tăng tồn và ledger một lần theo lượng nhận mới.
- Quản trị sản phẩm/biến thể, ảnh cục bộ, danh mục, thương hiệu, nhà cung cấp, coupon, nhiều vai trò và điều chỉnh kho có lý do.
- Bổ sung xuất báo cáo CSV, bảo vệ tài khoản Admin đang dùng, kiểm tra số lượng và biểu mẫu.

## Kiểm chứng trên trình duyệt thật

Dùng Playwright với Brave headless trong một phiên kiểm thử riêng; không điều khiển trình duyệt cá nhân.

- 57 màn × 3 độ rộng **1440 / 768 / 390px** = **171 lượt render**.
- Không có lỗi JavaScript, trang trống, ảnh hỏng, trang tràn ngang hoặc button data-action thiếu handler.
- Bốn weight font được kiểm tra ở trạng thái loaded.
- Đã thao tác thực các luồng: chọn biến thể → giỏ → coupon → checkout; chuyển trạng thái đơn → thu COD; nhận một phần → nhận đủ; điều chỉnh kho; hồ sơ; kiểm duyệt; lọc; tư vấn; VNPay thất bại → thử lại → thành công; menu mobile và Escape.
- Đã kiểm tra thêm: thêm/xóa địa chỉ; lưu sản phẩm; tạo danh mục/coupon/nhà cung cấp; đa vai trò; bảo vệ Admin; tạo/hủy phiếu; từ chối tồn âm; hủy đơn hoàn kho; xuất CSV; từ chối tên chỉ có khoảng trắng; dữ liệu tồn tại sau reload.

Bằng chứng: **qa/report.json**, **qa/extra-report.json** và ảnh chụp trong **qa/**.

## Giới hạn dữ liệu

Đây là prototype độc lập, chưa kết nối backend/database. Thanh toán VNPay, đăng nhập/đăng ký, đổi mật khẩu và tư vấn AI là mô phỏng. Không dùng mật khẩu thật; mật khẩu không được lưu. Biểu đồ xu hướng được ghi nhãn là dữ liệu minh họa; bảng và chỉ số nghiệp vụ chính lấy từ dữ liệu demo hiện tại.

Bản HTML mới không tự đồng bộ ngược vào file Figma/plugin cũ. Bản HTML trước khi sửa được giữ tại **preview-legacy.html**.

## Mã nguồn và tạo lại

Nguồn: **html/app.css**, **html/app.js**, **html/runtime.js**. Assets: **figma-assets/**. Font và giấy phép SIL OFL: **html/fonts/**; giấy phép cũng được nhúng vào HTML.

Chạy từ thư mục LaptopStore:

    node design/html/build.cjs

Lệnh cũ `node design/figma-build/render-preview.cjs` cũng tạo bản HTML mới này.

Kiểm thử trên môi trường hiện tại:

    node design/html/qa.cjs
    node design/html/qa-extra.cjs

Không sửa code ứng dụng frontend/backend trong tác vụ này.
