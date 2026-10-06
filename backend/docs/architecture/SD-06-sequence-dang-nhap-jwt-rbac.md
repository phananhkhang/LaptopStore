# SD-06: Sequence Diagram — Quy trình Đăng nhập (Login) + JWT/RBAC

> **Mã thiết kế:** SD-06  
> **Module:** `auth`, `user`, `security`  
> **Trạng thái:** Accepted (Đã được duyệt trước khi implement)  
> **Người thực hiện:** Huy  
> **Reviewer / Phê duyệt:** Khang

---

## 1. Biểu đồ tuần tự (Mermaid Sequence Diagram)

```mermaid
sequenceDiagram
    autonumber
    actor User as Người dùng
    participant FE as Frontend (LoginPage / authApi)
    participant AC as AuthController
    participant AS as AuthServiceImpl
    participant UCS as UserCredentialServiceImpl
    participant PE as PasswordEncoder (BCrypt)
    participant DB as Database (PostgreSQL)
    participant JWT as JwtTokenProvider & RefreshTokenService
    participant SEC as JwtAuthenticationFilter & SecurityContext

    %% Giai đoạn 1: Đăng nhập
    rect rgb(240, 248, 255)
    note right of User: Giai đoạn 1: Đăng nhập & Cấp phát Token
    User->>FE: Nhập email, password & bấm "Đăng nhập"
    FE->>FE: Validate email & password không được để trống
    FE->>AC: POST /api/v1/auth/login (LoginRequest)
    AC->>AS: login(LoginRequest)
    AS->>UCS: findByEmail(email)
    UCS->>DB: userRepository.findByEmailIgnoreCase(email)
    alt Email không tồn tại trong DB
        DB-->>UCS: Optional.empty()
        UCS-->>AS: empty
        AS-->>AC: throw BusinessException(401, "INVALID_CREDENTIALS")
        AC-->>FE: 401 Unauthorized ("Email hoặc mật khẩu không chính xác")
        FE-->>User: Thông báo lỗi thông tin đăng nhập sai
    else Email tồn tại
        DB-->>UCS: User entity
        UCS-->>AS: UserCredentialDto
        alt Trạng thái tài khoản != ACTIVE (bị khóa hoặc suspend)
            AS-->>AC: throw BusinessException(403, "ACCOUNT_INACTIVE")
            AC-->>FE: 403 Forbidden ("Tài khoản đã bị tạm khóa")
            FE-->>User: Thông báo tài khoản bị khóa, liên hệ Admin
        else Tài khoản ACTIVE
            AS->>PE: matches(rawPassword, credential.getPasswordHash())
            alt Mật khẩu không khớp
                PE-->>AS: false
                AS-->>AC: throw BusinessException(401, "INVALID_CREDENTIALS")
                AC-->>FE: 401 Unauthorized ("Email hoặc mật khẩu không chính xác")
                FE-->>User: Thông báo lỗi thông tin đăng nhập sai
            else Mật khẩu khớp chính xác
                PE-->>AS: true
                AS->>JWT: generateAccessToken(CustomUserDetails có roles: USER, STAFF, ADMIN)
                JWT-->>AS: accessToken (chứa claims: sub, userId, roles, tokenVersion)
                AS->>JWT: createRefreshToken(userId)
                JWT->>DB: Lưu refresh token hash mới (family rotation)
                DB-->>JWT: Persisted
                JWT-->>AS: rawRefreshToken
                AS-->>AC: AuthResult(AuthResponse, rawRefreshToken)
                AC->>AC: Set-Cookie: refresh_token (HttpOnly, Path=/api/v1/auth)
                AC-->>FE: 200 OK (AuthResponse DTO)
                FE->>FE: setAccessToken(accessToken), cập nhật user profile trong AuthContext
                FE-->>User: Điều hướng về trang trước đó hoặc Dashboard tương ứng role
            end
        end
    end
    end

    %% Giai đoạn 2: Gọi Protected API & Phân quyền RBAC
    rect rgb(245, 255, 245)
    note right of User: Giai đoạn 2: Xác thực & Phân quyền RBAC trên từng Request
    User->>FE: Truy cập trang quản trị hoặc nghiệp vụ (VD: /staff/orders)
    FE->>SEC: GET /api/v1/staff/orders (Header: "Authorization: Bearer <accessToken>")
    SEC->>SEC: JwtAuthenticationFilter intercept request
    SEC->>JWT: validateToken(accessToken)
    alt Token không hợp lệ hoặc hết hạn
        JWT-->>SEC: false / ExpiredJwtException
        SEC-->>FE: 401 Unauthorized (JwtAuthenticationEntryPoint)
        FE->>AC: Tự động gọi POST /api/v1/auth/refresh (gửi kèm HttpOnly cookie)
    else Token hợp lệ
        JWT-->>SEC: true (Claims trích xuất thành công)
        SEC->>SEC: Kiểm tra tokenVersion & nạp authorities (ROLE_USER, ROLE_STAFF...)
        SEC->>SEC: SecurityContextHolder.setAuthentication(auth)
        alt Endpoint yêu cầu ROLE_STAFF hoặc ROLE_ADMIN mà user chỉ có ROLE_USER
            SEC-->>FE: 403 Forbidden (CustomAccessDeniedHandler: ACCESS_DENIED)
            FE-->>User: Điều hướng sang trang /403 (Không có quyền truy cập)
        else Role hợp lệ
            SEC->>DB: Cho phép request đi tới Controller & Service thực thi
            DB-->>FE: 200 OK (Dữ liệu đơn hàng)
            FE-->>User: Hiển thị giao diện danh sách đơn hàng
        end
    end
    end
```

---

## 2. Nguyên tắc xác thực & RBAC

1. **Phòng chống Account Enumeration:** Cả hai trường hợp sai email hoặc sai mật khẩu đều trả về cùng mã lỗi `401 UNAUTHORIZED` với message chung "Invalid email or password" (`INVALID_CREDENTIALS`).
2. **Kiểm tra trạng thái tài khoản thời gian thực:** Kiểm tra `status == 'ACTIVE'` khi đăng nhập và đối chiếu `tokenVersion` khi lọc JWT để vô hiệu hóa ngay phiên làm việc khi tài khoản bị khóa.
3. **Phân quyền đa vai trò (Multi-Role):**
   - User có thể sở hữu đồng thời nhiều role: `ROLE_USER`, `ROLE_STAFF`, `ROLE_ADMIN`.
   - Các API `/staff/**` yêu cầu có `ROLE_STAFF` hoặc `ROLE_ADMIN`.
   - Các API `/admin/**` chỉ chấp nhận `ROLE_ADMIN`.
   - Quyền hạn được kiểm tra cả ở tầng Filter (`SecurityConfig`) và tầng Service qua `@PreAuthorize`.
