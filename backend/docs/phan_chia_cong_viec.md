# Phân chia công việc nhóm LaptopStore

| Thành viên | Module phụ trách | Công việc chính |
|---|---|---|
| **A** | `auth`, `user`, `security`, `config`, `exception`, `common` | Đăng ký/đăng nhập, JWT–Refresh Token, quản lý tài khoản/địa chỉ, phân quyền `USER`–`STAFF`–`ADMIN`, xử lý lỗi và cấu hình dùng chung |
| **B** | `catalog`, `inventory`, `procurement` | Quản lý sản phẩm/biến thể, brand/category, tồn kho và lịch sử biến động kho, nhà cung cấp và phiếu nhập hàng |
| **C** | `cart`, `order` | Giỏ hàng, checkout, tạo/hủy đơn, quản lý trạng thái đơn, lịch sử đơn và điều phối tồn kho/coupon |
| **D** | `payment`, `promotion`, `review` | COD/thanh toán online, callback thanh toán, quản lý coupon, lượt sử dụng coupon và đánh giá sản phẩm sau mua |