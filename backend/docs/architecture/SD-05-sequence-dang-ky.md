# SD-05: Sequence Diagram — Quy trình Đăng ký (Register)

> **Mã thiết kế:** SD-05  
> **Module:** `auth`, `user`, `security`  
> **Trạng thái:** Accepted (Đã được duyệt trước khi implement)  
> **Người thực hiện:** Huy  
> **Reviewer / Phê duyệt:** Khang

---

## 1. Biểu đồ tuần tự (Mermaid Sequence Diagram)

```mermaid
sequenceDiagram
    autonumber
    actor User as Khách hàng (User)
    participant FE as Frontend (RegisterPage / authApi)
    participant AC as AuthController
    participant AS as AuthServiceImpl
    participant UCS as UserCredentialServiceImpl
    participant PE as PasswordEncoder (BCrypt)
    participant DB as Database (PostgreSQL)
    participant JWT as JwtTokenProvider & RefreshTokenService

    User->>FE: Nhập họ tên, email, mật khẩu, số điện thoại & bấm "Đăng ký"
    FE->>FE: Client-side validate (email hợp lệ, password >= 6 ký tự, mật khẩu xác nhận khớp)
    alt Validate form thất bại tại FE
        FE-->>User: Hiển thị lỗi form trực tiếp tại trường tương ứng
    else Form hợp lệ
        FE->>AC: POST /api/v1/auth/register (RegisterRequest)
        AC->>AC: Spring Validation (@Valid)
        alt Payload không hợp lệ (email sai định dạng, password trống)
            AC-->>FE: 400 Bad Request (VALIDATION_ERROR + fieldErrors)
            FE-->>User: Hiển thị lỗi từng trường cụ thể
        else Payload hợp lệ
            AC->>AS: register(RegisterRequest)
            AS->>PE: encode(request.getPassword())
            PE-->>AS: hashedPassword (chuỗi $2a$...)
            AS->>UCS: createUser(email, hashedPassword, fullName, phone, roles)
            UCS->>DB: userRepository.existsByEmailIgnoreCase(normalizedEmail)
            alt Email đã tồn tại trong DB
                DB-->>UCS: true
                UCS-->>AS: throw BusinessException(409, "DUPLICATE_RESOURCE")
                AS-->>AC: Bubble exception
                AC-->>FE: 409 Conflict ("Email already registered")
                FE-->>User: Báo lỗi "Email đã được sử dụng"
            else Email chưa tồn tại
                DB-->>UCS: false
                UCS->>DB: Lưu User mới (status='ACTIVE', password_hash=hashedPassword, role='ROLE_USER')
                DB-->>UCS: User entity đã lưu
                UCS-->>AS: UserCredentialDto (không chứa plain password)
                AS->>JWT: generateAccessToken(userDetails)
                JWT-->>AS: accessToken
                AS->>JWT: createRefreshToken(userId)
                JWT->>DB: Lưu hashed refresh token vào bảng refresh_tokens
                DB-->>JWT: Token persisted
                JWT-->>AS: rawRefreshToken
                AS-->>AC: AuthResult(AuthResponse, rawRefreshToken)
                Note over AC: AuthResponse chỉ gồm: userId, email, fullName, roles, accessToken.<br/>TUYỆT ĐỐI KHÔNG có passwordHash!
                AC->>AC: Ghi rawRefreshToken vào HttpOnly, Secure, SameSite Cookie
                AC-->>FE: 201 Created (AuthResponse DTO + Set-Cookie)
                FE->>FE: Lưu accessToken trong state, cập nhật AuthContext
                FE-->>User: Chuyển hướng sang trang chủ hoặc Profile
            end
        end
    end
```

---

## 2. Các quy tắc bảo mật & chuẩn hóa

1. **Chuẩn hóa Email:** Email được `trim()` và chuyển thành chữ thường (`toLowerCase()`) trước khi kiểm tra trùng lặp và lưu trữ.
2. **Mã hóa mật khẩu:**
   - Mật khẩu thuần được băm qua `BCryptPasswordEncoder` ngay tại tầng Service trước khi chuyển sang `UserCredentialService`.
   - Chuỗi hash bắt đầu bằng `$2a$` hoặc `$2b$` với độ an toàn cao.
3. **Không rò rỉ dữ liệu nhạy cảm:**
   - `AuthResponse` DTO chỉ chứa: `userId`, `email`, `fullName`, `roles`, `accessToken`.
   - Không xuất hiện `password` hay `passwordHash` trong bất kỳ response, error body hay log application.
4. **Bảo vệ Refresh Token:**
   - Refresh token dạng raw chỉ truyền về client qua **HttpOnly Cookie** (`path=/api/v1/auth`, `SameSite=Lax/Strict`, `Secure` khi bật HTTPS).
   - Database chỉ lưu hash của refresh token (`token_hash`) để phòng chống rò rỉ database.
