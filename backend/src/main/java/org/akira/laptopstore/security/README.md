# security

**Owner đề xuất:** A; chưa gán tên. **Trạng thái:** mới có hướng dẫn, chưa có class.

## Cần viết

SecurityConfig, JwtAuthenticationFilter, JwtService, CustomUserDetailsService, CurrentUser/Principal.

Xác thực request, kiểm token, tạo principal, route/method authorization. SecurityConfig ở đây; cấu hình CORS dùng một nguồn thống nhất với config.

## Ràng buộc

Chỉ nạp account qua user.service; không gọi auth để tránh vòng auth → security → auth. Filter không làm checkout/CRUD tài khoản. EntryPoint/AccessDeniedHandler trả lỗi thống nhất; ownership vẫn kiểm trong service.

## Kiểm tra và phối hợp

Bearer hết hạn/sai; 401/403; tài khoản khóa với token cũ; role thay đổi; request public; CSRF refresh/logout.

Đọc [ownership](../../../../../../../docs/architecture/03-ownership.md), [dependency](../../../../../../../docs/architecture/04-dependencies.md) và [bảo mật](../../../../../../../docs/architecture/08-security.md).
