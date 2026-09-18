# Test module cart

**Owner:** C. Đọc [quy tắc module](../../../../../../main/java/org/akira/laptopstore/cart/README.md). Thư mục mới chỉ có kế hoạch kiểm tra, chưa có test nghiệp vụ.

## Hành vi cần chứng minh

quantity 0/âm; hai lần thêm cùng SKU không tạo dòng trùng; không sửa giỏ người khác; checkout đồng thời cập nhật giỏ.

Unit test cho service/quy tắc; web test cho DTO/status/role; repository test dùng PostgreSQL cho query/constraint/lock. Luồng nhiều module đặt ở integration và phối hợp owner contract. Không viết test để mirror getter hay công bố concurrency bằng test đơn luồng.

Tên class `*Test`/`*Tests` phù hợp runner hiện tại; `*IT` chỉ dùng khi cấu hình thêm Failsafe. Mỗi test dữ liệu độc lập, không phụ thuộc thứ tự hoặc DB cá nhân. Ghi lệnh/kết quả thật trong PR.
