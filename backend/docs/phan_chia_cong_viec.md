# Phân chia công việc nhóm LaptopStore

| Thành viên | Module phụ trách | Công việc chính |
|---|---|---|
| **A** | `auth`, `user`, `security`, `config`, `exception`, `common` | Đăng ký/đăng nhập, JWT–Refresh Token, quản lý tài khoản/địa chỉ, phân quyền `USER`–`STAFF`–`ADMIN`, xử lý lỗi và cấu hình dùng chung |
| **B** | `catalog`, `inventory`, `procurement` | Sản phẩm/biến thể và thông số; `inventory_stocks` + `stock_movements`; nhà cung cấp và phiếu nhập hàng |
| **C** | `cart`, `order` | Giỏ hàng, checkout, tạo/hủy đơn, trạng thái/lịch sử đơn, snapshot và điều phối tồn kho/coupon |
| **D** | `payment`, `promotion`, `review` | COD/thanh toán online, callback thanh toán, coupon/lượt sử dụng và đánh giá sản phẩm sau mua |
