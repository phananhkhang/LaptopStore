# Test module user

**Owner:** A. Đọc [quy tắc module](../../../../../../main/java/org/akira/laptopstore/user/README.md). Thư mục mới chỉ có kế hoạch kiểm tra, chưa có test nghiệp vụ.

## Hành vi cần chứng minh

email khác hoa thường bị trùng; sửa địa chỉ người khác; mass assignment role; khóa tài khoản ảnh hưởng truy cập.

Unit test cho service/quy tắc; web test cho DTO/status/role; repository test dùng PostgreSQL cho query/constraint/lock. Luồng nhiều module đặt ở integration và phối hợp owner contract. Không viết test để mirror getter hay công bố concurrency bằng test đơn luồng.

Tên class `*Test`/`*Tests` phù hợp runner hiện tại; `*IT` chỉ dùng khi cấu hình thêm Failsafe. Mỗi test dữ liệu độc lập, không phụ thuộc thứ tự hoặc DB cá nhân. Ghi lệnh/kết quả thật trong PR.
