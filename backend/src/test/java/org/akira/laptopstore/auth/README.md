# Test module auth

**Owner:** A. Đọc [quy tắc module](../../../../../../main/java/org/akira/laptopstore/auth/README.md). Thư mục mới chỉ có kế hoạch kiểm tra, chưa có test nghiệp vụ.

## Hành vi cần chứng minh

email trùng; sai mật khẩu; tài khoản khóa; hai refresh đồng thời; token hết hạn/thu hồi; logout rồi refresh bị từ chối.

Unit test cho service/quy tắc; web test cho DTO/status/role; repository test dùng PostgreSQL cho query/constraint/lock. Luồng nhiều module đặt ở integration và phối hợp owner contract. Không viết test để mirror getter hay công bố concurrency bằng test đơn luồng.

Tên class `*Test`/`*Tests` phù hợp runner hiện tại; `*IT` chỉ dùng khi cấu hình thêm Failsafe. Mỗi test dữ liệu độc lập, không phụ thuộc thứ tự hoặc DB cá nhân. Ghi lệnh/kết quả thật trong PR.
