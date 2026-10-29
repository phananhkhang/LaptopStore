import { ReactNode, useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { isAxiosError } from "axios";
import { apiClient } from "@/services/api/apiClient";
import { OrderItem, OrderStatus } from "../types";
import { Icon } from "./CommerceLayout";

export const money = (n: number) =>
  new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(n);
export const dateTime = (value: string) =>
  new Date(value).toLocaleString("vi-VN");
export const statusLabels: Record<OrderStatus, string> = {
  CREATED: "Khởi tạo",
  PENDING_PAYMENT: "Chờ thanh toán",
  WAITING_CONFIRMATION: "Chờ xác nhận",
  CONFIRMED: "Đã xác nhận",
  PACKING: "Đang đóng gói",
  SHIPPING: "Đang giao",
  DELIVERED: "Đã giao",
  DELIVERY_FAILED: "Giao thất bại",
  CANCELLED: "Đã hủy",
  RETURNED: "Đã trả hàng",
};
export function errorMessage(
  error: unknown,
  fallback = "Không thể hoàn tất thao tác. Vui lòng thử lại.",
) {
  if (isAxiosError(error)) {
    const data = error.response?.data as { message?: string } | undefined;
    return data?.message || fallback;
  }
  return error instanceof Error ? error.message : fallback;
}
export const Alert = ({
  children,
  error = true,
}: {
  children: ReactNode;
  error?: boolean;
}) => (
  <div
    className={`alert ${error ? "error" : ""}`}
    role={error ? "alert" : "status"}
  >
    {children}
  </div>
);
export const Loading = () => (
  <div className="empty" role="status" aria-live="polite">
    Đang tải dữ liệu...
  </div>
);
export const StatusBadge = ({ status }: { status: OrderStatus }) => (
  <span
    className={`badge ${status === "DELIVERED" ? "green" : ["PACKING", "SHIPPING"].includes(status) ? "blue" : ["CANCELLED", "RETURNED"].includes(status) ? "gray" : status === "DELIVERY_FAILED" ? "red" : ""}`}
  >
    {statusLabels[status] || status}
  </span>
);

const imageCache = new Map<number, Promise<string | undefined>>();
export function ProductVisual({
  productId,
  className = "",
}: {
  productId?: number;
  className?: string;
}) {
  const [url, setUrl] = useState<string>();
  useEffect(() => {
    setUrl(undefined);
    if (!productId) return;
    let active = true;
    if (!imageCache.has(productId))
      imageCache.set(
        productId,
        apiClient
          .get(`/products/${productId}`)
          .then(({ data }) => {
            const images = data.images as {
              imageUrl: string;
              isPrimary: boolean;
            }[];
            return (images?.find((i) => i.isPrimary) || images?.[0])?.imageUrl;
          })
          .catch(() => undefined),
      );
    void imageCache.get(productId)!.then((value) => {
      if (active) setUrl(value);
    });
    return () => {
      active = false;
    };
  }, [productId]);
  return (
    <img
      className={`product-visual ${className}`}
      src={url || "/commerce/product-dark.svg"}
      alt=""
      onError={() => setUrl(undefined)}
    />
  );
}
export function OrderItems({ items }: { items: OrderItem[] }) {
  return (
    <div className="stack">
      {items.map((item) => (
        <div
          className="product-row compact-product"
          key={item.id || item.variantId}
        >
          <ProductVisual productId={item.productId} />
          <div className="product-copy">
            <h3>{item.productName}</h3>
            <p>{item.variantName}</p>
            <small>
              SKU {item.sku} · Số lượng {item.quantity}
            </small>
            <strong>{money(item.lineTotal)}</strong>
          </div>
        </div>
      ))}
    </div>
  );
}
export function Totals({
  subtotal,
  discountAmount,
  shippingFee,
  totalAmount,
  couponCode,
  children,
}: {
  subtotal: number;
  discountAmount: number;
  shippingFee: number;
  totalAmount: number;
  couponCode?: string | null;
  children?: ReactNode;
}) {
  return (
    <section className="panel summary">
      <h2>Tóm tắt đơn hàng</h2>
      <div className="summary-row">
        <span>Tạm tính</span>
        <strong>{money(subtotal)}</strong>
      </div>
      <div className="summary-row">
        <span>Giảm giá {couponCode}</span>
        <strong className="green">−{money(discountAmount)}</strong>
      </div>
      <div className="summary-row">
        <span>Phí vận chuyển</span>
        <strong>{shippingFee ? money(shippingFee) : "Miễn phí"}</strong>
      </div>
      <hr className="divider" />
      <p className="muted">Tổng thanh toán</p>
      <strong className="total">{money(totalAmount)}</strong>
      {children}
      <p className="secure-note">
        <Icon name="shield" />
        Thông tin của bạn luôn được bảo vệ
      </p>
    </section>
  );
}
export function OrderProgress({ status }: { status: OrderStatus }) {
  const steps = ["Đặt hàng", "Xác nhận", "Đóng gói", "Đang giao", "Đã giao"];
  const index =
    status === "DELIVERED"
      ? 4
      : ["SHIPPING", "DELIVERY_FAILED"].includes(status)
        ? 3
        : status === "PACKING"
          ? 2
          : status === "CONFIRMED"
            ? 1
            : 0;
  return (
    <section className="panel">
      <h2>Hành trình đơn hàng</h2>
      <div className="alert" style={{ marginTop: 16, marginBottom: 0 }}>
        <strong>{statusLabels[status]}</strong>
        <p className="muted">
          {status === "PENDING_PAYMENT"
            ? "Đơn online được giữ trong 15 phút. Vui lòng thanh toán trước khi hết hạn."
            : status === "WAITING_CONFIRMATION"
              ? "Cửa hàng đang tiếp nhận đơn của bạn."
              : status === "DELIVERY_FAILED"
                ? "Cửa hàng sẽ liên hệ để sắp xếp giao lại."
                : status === "CANCELLED"
                  ? "Đơn đã hủy. Tồn kho và lượt dùng ưu đãi đã được hoàn lại."
                  : "Đơn hàng của bạn được cập nhật ở từng chặng đường."}
        </p>
      </div>
      {!["CANCELLED", "RETURNED"].includes(status) && (
        <ol className="progress">
          {steps.map((label, i) => (
            <li className={i <= index ? "done" : ""} key={label}>
              <span className="step-dot">{i <= index ? "✓" : i + 1}</span>
              {label}
            </li>
          ))}
        </ol>
      )}
    </section>
  );
}
export function ConfirmDialog({
  open,
  busy,
  title,
  children,
  confirmLabel,
  onClose,
  onConfirm,
}: {
  open: boolean;
  busy: boolean;
  title: string;
  children: ReactNode;
  confirmLabel: string;
  onClose: () => void;
  onConfirm: () => void;
}) {
  const ref = useRef<HTMLDialogElement>(null);
  useEffect(() => {
    if (open && !ref.current?.open) ref.current?.showModal();
    if (!open && ref.current?.open) ref.current.close();
  }, [open]);
  return (
    <dialog
      ref={ref}
      aria-labelledby="confirm-title"
      onCancel={(e) => {
        e.preventDefault();
        if (!busy) onClose();
      }}
    >
      <h2 id="confirm-title">{title}</h2>
      <div style={{ marginTop: 16 }} className="muted">
        {children}
      </div>
      <div className="dialog-actions">
        <button className="button" disabled={busy} onClick={onClose}>
          Quay lại
        </button>
        <button className="button primary" disabled={busy} onClick={onConfirm}>
          {busy ? "Đang xử lý..." : confirmLabel}
        </button>
      </div>
    </dialog>
  );
}
export function Pagination({
  page,
  totalPages,
  total,
  onPage,
}: {
  page: number;
  totalPages: number;
  total: number;
  onPage: (n: number) => void;
}) {
  return (
    <div className="pagination">
      <span>
        {total} kết quả · Trang {Math.min(page + 1, Math.max(1, totalPages))}/
        {Math.max(1, totalPages)}
      </span>
      <div>
        <button
          className="button"
          disabled={page <= 0}
          onClick={() => onPage(page - 1)}
        >
          Trang trước
        </button>
        <button
          className="button"
          disabled={page + 1 >= totalPages}
          onClick={() => onPage(page + 1)}
        >
          Trang sau
        </button>
      </div>
    </div>
  );
}
export const EmptyOrders = () => (
  <div className="empty panel">
    <h2>Chưa có đơn hàng phù hợp</h2>
    <p className="muted">
      Thử thay đổi bộ lọc hoặc khám phá chiếc laptop dành cho bạn.
    </p>
    <Link className="button primary" to="/products">
      Khám phá laptop
    </Link>
  </div>
);
