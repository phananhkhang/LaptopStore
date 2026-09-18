# Bảo mật và quyền truy cập

Thiết kế này chưa được cài đặt trong source. Security/JWT/validation cần dependency và cấu hình tương thích với version trong pom trước khi dùng annotation hoặc viết filter.

## Role và ownership

| Hành động | Public | USER | STAFF | ADMIN |
|---|---|---|---|---|
| Xem catalog/review public | Có | Có | Có | Có |
| Sửa profile/địa chỉ mình | Không | Có | Có | Có |
| Giỏ, mua và đánh giá | Không | Có | Chỉ nếu có thêm USER | Chỉ nếu có thêm USER |
| Xử lý đơn/nhập hàng/thu COD | Không | Không | Có | Có |
| CRUD catalog/coupon/supplier | Không | Không | Không trong MVP | Có |
| Điều chỉnh kho thủ công | Không | Không | Không trong MVP | Có |
| Đổi role/khóa tài khoản | Không | Không | Không | Có |

`/staff/**` cho cả STAFF/ADMIN; không giả định framework tự kế thừa role. `/me` mang nghĩa tài nguyên của principal, không đồng nghĩa mọi `/me/**` đều cho mọi role. Chặn mặc định các endpoint ngoài allowlist đã định nghĩa.

Controller kiểm tra xác thực/role; service luôn kiểm tra quyền sở hữu khi đọc/ghi ID cá nhân. Quy ước tài nguyên cá nhân của người khác trả 404 để tránh tiết lộ tồn tại. Không lấy actor từ body/query.

## Token và tài khoản

- Mật khẩu hash qua PasswordEncoder; không plaintext hoặc tự thiết kế thuật toán hash.
- JWT access gửi Bearer; kiểm tra chữ ký, hạn và các claim được cấu hình. TTL access/refresh phải được nhóm thống nhất và cấu hình, không hard-code khắp code.
- Đề xuất access 15 phút, refresh 7 ngày; raw refresh chỉ ở HttpOnly cookie, DB lưu hash. Cookie Secure khi HTTPS; Path/SameSite phù hợp môi trường frontend/backend.
- Rotate refresh dưới khóa, mark used và tạo token mới atomic; logout revoke session. Family/reuse detection nâng cao ngoài baseline, nếu bổ sung phải xử lý rollback khi thu hồi và throw exception.
- Để khóa tài khoản/đổi role có hiệu lực ngay ở MVP, security nạp user status/role hiện tại mỗi request xác thực. Không chỉ tin role cũ trong JWT rồi giả rằng khóa user đã hiệu lực.

## Cookie, CORS và CSRF

Bearer API và refresh cookie có đặc tính khác nhau. Browser tự gửi cookie nên refresh/logout cần CSRF protection hoặc kiểm tra Origin kèm biện pháp token phù hợp với topology. Không tắt CSRF toàn hệ thống chỉ vì dùng JWT. CORS giới hạn origin cụ thể, credentials chỉ khi cần; CORS không thay xác thực hoặc bảo vệ CSRF. Chốt cùng người làm frontend trước triển khai.

## Payment và dữ liệu nhạy cảm

Chỉ server xác nhận kết quả payment sau xác minh provider. Endpoint mô phỏng profile dev/test không được tồn tại ở prod. Lưu reference/amount/result cần thiết, không log payload chứa bí mật. Không đưa JWT signing key, DB password, provider secret, raw token hoặc thông tin thẻ vào repository, DTO hay README.

Ảnh upload khi được thêm cần whitelist loại/kích thước, tên/object key do server sinh, kiểm tra quyền owner và không dùng tên file client để ghi tùy ý đường dẫn. Object storage chưa được chọn trong MVP hiện tại.

## Kiểm tra trước bàn giao

Test public/401/403/ownership 404; register gửi role ADMIN; DTO cập nhật profile chứa trường cấm; user bị khóa với JWT cũ; refresh race; callback sai chữ ký; redirect browser giả thành công. Kiểm tra cả filter và service, không chỉ route matcher.
