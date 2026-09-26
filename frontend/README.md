# Frontend LaptopStore

Khung frontend được tổ chức theo **feature-first** cho React + TypeScript. Mỗi nghiệp vụ nằm trong `src/modules`; phần dùng chung đặt ở các thư mục cấp `src` để tránh phụ thuộc vòng và tránh sao chép code giữa các module.

## Cấu trúc

```text
frontend/
├── public/                         # Tài nguyên được phục vụ nguyên trạng
├── src/
│   ├── app/
│   │   ├── providers/              # Router, query, theme, auth providers
│   │   └── router/                 # Router gốc, guards, route constants
│   ├── assets/
│   │   ├── fonts/
│   │   ├── icons/
│   │   └── images/
│   ├── components/                 # Component dùng chung toàn ứng dụng
│   │   ├── common/
│   │   ├── data-display/
│   │   ├── feedback/
│   │   ├── forms/
│   │   └── navigation/
│   ├── config/                     # Cấu hình runtime và biến môi trường
│   ├── constants/                  # Hằng số dùng chung
│   ├── hooks/                      # Hook dùng chung
│   ├── layouts/                    # Public, account, staff, admin layouts
│   ├── lib/                        # Khởi tạo thư viện bên thứ ba
│   ├── modules/
│   │   ├── auth/                   # Đăng nhập, đăng ký, refresh/logout
│   │   ├── user/                   # Hồ sơ, địa chỉ, quản lý tài khoản
│   │   ├── catalog/                # Danh mục, sản phẩm, biến thể
│   │   ├── cart/                   # Giỏ hàng
│   │   ├── order/                  # Checkout, lịch sử và xử lý đơn hàng
│   │   ├── payment/                # Trạng thái và phương thức thanh toán
│   │   ├── promotion/              # Coupon/khuyến mãi
│   │   ├── review/                 # Đánh giá sản phẩm
│   │   ├── inventory/              # Quản lý tồn kho cho staff/admin
│   │   ├── procurement/            # Nhập hàng và nhà cung cấp
│   │   └── dashboard/              # Tổng quan theo vai trò
│   ├── services/
│   │   ├── api/                    # HTTP client, interceptor, error mapping
│   │   └── storage/                # Adapter local/session storage
│   ├── store/                      # Global store thực sự dùng chung
│   ├── styles/                     # Global styles, theme, design tokens
│   ├── test/
│   │   ├── fixtures/
│   │   ├── mocks/
│   │   └── utils/
│   ├── types/                      # Kiểu dữ liệu dùng chung
│   └── utils/                      # Hàm thuần dùng chung
└── tests/e2e/                      # Kiểm thử luồng người dùng đầu-cuối
```

## Quy ước module

Một module có thể dùng các thư mục sau và chỉ giữ lại thư mục thực sự cần:

- `api`: hàm gọi endpoint và query/mutation options.
- `components`: component chỉ thuộc feature đó.
- `hooks`: hook điều phối trạng thái/UI của feature.
- `pages`: màn hình route-level; có thể chia `user`, `staff`, `admin`.
- `routes`: route con của feature.
- `schemas`: validation schema cho form/request.
- `store`: client state cục bộ của feature, không chứa server state nếu dùng query cache.
- `types`: DTO/view model thuộc feature.

Module không import file nội bộ của module khác. Dữ liệu dùng chung đi qua public export của module hoặc được nâng lên thư mục dùng chung phù hợp. Quyền, giá tiền, trạng thái đơn hàng và tồn kho từ backend luôn là nguồn dữ liệu authoritative; frontend chỉ hiển thị và gửi yêu cầu.

Luồng checkout thuộc module `order` và được đặt tại `src/modules/order/pages/checkout` để ranh giới nghiệp vụ frontend đồng nhất với backend.
