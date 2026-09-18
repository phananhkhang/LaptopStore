# Test module payment

**Owner:** D. Đọc [quy tắc module](../../../../../../main/java/org/akira/laptopstore/payment/README.md). Thư mục mới chỉ có kế hoạch kiểm tra, chưa có test nghiệp vụ.

## Hành vi cần chứng minh

sai chữ ký/số tiền; callback lặp; callback trễ sau hủy; timeout provider; retry session; user tự đánh dấu PAID; COD bị ghi hai lần.

Unit test cho service/quy tắc; web test cho DTO/status/role; repository test dùng PostgreSQL cho query/constraint/lock. Luồng nhiều module đặt ở integration và phối hợp owner contract. Không viết test để mirror getter hay công bố concurrency bằng test đơn luồng.

Tên class `*Test`/`*Tests` phù hợp runner hiện tại; `*IT` chỉ dùng khi cấu hình thêm Failsafe. Mỗi test dữ liệu độc lập, không phụ thuộc thứ tự hoặc DB cá nhân. Ghi lệnh/kết quả thật trong PR.
