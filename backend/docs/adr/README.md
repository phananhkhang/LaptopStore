# Architecture Decision Records (ADR)

Thư mục này lưu các quyết định kiến trúc quan trọng của dự án.

## Khi nào cần ADR?

Tạo ADR khi thay đổi liên quan đến:

* Cấu trúc hoặc dependency giữa module.
* Entity / quan hệ database quan trọng.
* Security.
* Transaction / locking.
* Luồng nghiệp vụ chính.
* Công nghệ hoặc thư viện quan trọng.

Các thay đổi nhỏ như rename, refactor, fix bug không cần ADR.

## Đặt tên

```text
ADR-{số}-{tên-ngắn-gọn}.md
```

Ví dụ:

```text
ADR-001-module-boundaries.md
ADR-002-inventory-stock.md
ADR-003-user-role-mapping.md
```

Số ADR tăng dần và không sử dụng lại.

## Trạng thái

* `Proposed`: đang đề xuất.
* `Accepted`: đã chấp nhận.
* `Rejected`: không chấp nhận.
* `Superseded`: đã được ADR khác thay thế.

## Template

```markdown
# ADR-XXX: Tên quyết định

- Status: Proposed
- Date: YYYY-MM-DD

## Context
Vấn đề hiện tại là gì?

## Decision
Quyết định thay đổi như thế nào?

## Reason
Tại sao chọn cách này?

## Consequences
Thay đổi này ảnh hưởng đến những gì?
```

## Quy trình

```text
Tạo ADR (Proposed)
        ↓
Team review
        ↓
Accepted / Rejected
        ↓
Cập nhật architecture docs
        ↓
Code
```

ADR đã `Accepted` không nên xóa hoặc viết lại lịch sử. Nếu quyết định mới thay thế quyết định cũ, tạo ADR mới và chuyển ADR cũ thành `Superseded`.
