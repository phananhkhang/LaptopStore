import { useCallback, useEffect, useRef, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";
import { isAxiosError } from "axios";
import { orderApi } from "../api/orderApi";
import { Payment, openVnpay, paymentApi } from "../api/paymentApi";
import { OrderDetail, OrderStatus } from "../types";
import { StaffLayout, StoreLayout } from "./CommerceLayout";
import {
  Alert,
  ConfirmDialog,
  Loading,
  OrderItems,
  OrderProgress,
  StatusBadge,
  Totals,
  dateTime,
  errorMessage,
  statusLabels,
} from "./OrderUI";

const actions: Partial<Record<OrderStatus, string>> = {
  CONFIRMED: "Xác nhận đơn",
  PACKING: "Chuẩn bị đơn",
  SHIPPING: "Bàn giao vận chuyển",
  DELIVERED: "Xác nhận đã giao",
  DELIVERY_FAILED: "Ghi nhận giao thất bại",
  CANCELLED: "Hủy đơn hàng",
  RETURNED: "Ghi nhận hàng trả",
};
const paymentLabels: Record<string, string> = {
  PENDING: "Chờ thanh toán",
  PAID: "Đã thanh toán",
  FAILED: "Thanh toán thất bại",
  CANCELLED: "Đã hủy",
  REQUIRES_RECONCILIATION: "Cần đối soát",
};

export function OrderDetailView({ staff = false }: { staff?: boolean }) {
  const { orderId } = useParams();
  const location = useLocation();
  const [order, setOrder] = useState<OrderDetail | null>(null);
  const [payment, setPayment] = useState<Payment | null>(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [paymentError, setPaymentError] = useState(
    location.state?.paymentError || "",
  );
  const [command, setCommand] = useState<OrderStatus | "COD" | null>(null);
  const lock = useRef(false);
  const sequence = useRef(0);
  const load = useCallback(async () => {
    const request = ++sequence.current;
    if (!orderId || !/^\d+$/.test(orderId)) {
      setError("Mã đơn hàng không hợp lệ.");
      setLoading(false);
      return;
    }
    setLoading(true);
    setError("");
    try {
      const data = await (staff
        ? orderApi.getStaffById(orderId)
        : orderApi.getMineById(orderId));
      if (sequence.current !== request) return;
      setOrder(data);
      try {
        const p = await (staff
          ? paymentApi.getForStaff(data.id)
          : paymentApi.getForOrder(data.id));
        if (sequence.current === request) {
          setPayment(p);
          setPaymentError("");
        }
      } catch (err) {
        if (sequence.current === request) {
          setPayment(null);
          if (!isAxiosError(err) || err.response?.status !== 404)
            setPaymentError(errorMessage(err));
        }
      }
    } catch (err) {
      if (sequence.current === request) {
        setOrder(null);
        setError(
          errorMessage(
            err,
            "Không tìm thấy đơn hàng hoặc bạn không có quyền xem.",
          ),
        );
      }
    } finally {
      if (sequence.current === request) setLoading(false);
    }
  }, [orderId, staff]);
  useEffect(() => {
    void load();
    return () => {
      ++sequence.current;
    };
  }, [load]);
  async function confirm() {
    if (!order || !command || lock.current) return;
    lock.current = true;
    setBusy(true);
    setError("");
    try {
      if (command === "COD") await paymentApi.collectCod(order.id);
      else if (staff) await orderApi.updateStatus(order.id, command);
      else await orderApi.cancel(order.id);
      setCommand(null);
      await load();
    } catch (err) {
      setCommand(null);
      await load();
      setError(errorMessage(err));
    } finally {
      lock.current = false;
      setBusy(false);
    }
  }
  async function pay() {
    if (!order || lock.current) return;
    lock.current = true;
    setBusy(true);
    setPaymentError("");
    try {
      await openVnpay(order.id);
    } catch (err) {
      setPaymentError(errorMessage(err));
    } finally {
      lock.current = false;
      setBusy(false);
    }
  }
  async function initializeCod() {
    if (!order || lock.current) return;
    lock.current = true;
    setBusy(true);
    try {
      await paymentApi.start(order.id, "COD");
      await load();
    } catch (err) {
      setPaymentError(errorMessage(err));
    } finally {
      lock.current = false;
      setBusy(false);
    }
  }
  const Layout = staff ? StaffLayout : StoreLayout;
  const canCancel =
    order && ["PENDING_PAYMENT", "WAITING_CONFIRMATION"].includes(order.status);
  return (
    <Layout>
      {error && (
        <Alert>
          {error}
          <button className="text-button" onClick={() => void load()}>
            Tải lại
          </button>
        </Alert>
      )}
      {loading ? (
        <Loading />
      ) : order ? (
        <>
          <div className="page-heading">
            <div>
              {staff && <p className="eyebrow">#{order.orderCode}</p>}
              <h1>Chi tiết đơn hàng</h1>
              <p>
                {staff
                  ? "Đặt lúc " + dateTime(order.createdAt)
                  : "Đơn hàng của bạn, cập nhật ở từng chặng đường."}
              </p>
            </div>
            <Link
              className="button ghost"
              to={staff ? "/staff/orders" : "/orders"}
            >
              Quay lại đơn hàng
            </Link>
          </div>
          {!staff && (
            <div className="page-heading">
              <h3>{order.orderCode}</h3>
              <StatusBadge status={order.status} />
            </div>
          )}
          <div className="columns detail">
            <div className="stack">
              {!staff && <OrderProgress status={order.status} />}
              <section className="panel stack">
                <h2>{staff ? "Sản phẩm trong đơn" : "Sản phẩm"}</h2>
                <OrderItems items={order.items} />
              </section>
              <section className="panel stack">
                <h3>Thông tin {staff ? "giao" : "nhận"} hàng</h3>
                <strong>
                  {order.recipientName} · {order.recipientPhone}
                </strong>
                <p className="muted">{order.shippingAddress}</p>
                {order.customerNote && (
                  <p className="muted">Ghi chú: {order.customerNote}</p>
                )}
              </section>
              {staff && (
                <Totals {...order} couponCode={order.couponCodeSnapshot} />
              )}
              <section className="panel stack">
                <h3>Lịch sử trạng thái</h3>
                <ol className="timeline">
                  {order.statusHistories.map((h) => (
                    <li key={h.id}>
                      <strong>{statusLabels[h.toStatus]}</strong>
                      <small>{dateTime(h.changedAt)}</small>
                    </li>
                  ))}
                </ol>
              </section>
            </div>
            <div className="stack">
              {staff && <OrderProgress status={order.status} />}
              <section className="panel stack">
                <h2>Thanh toán</h2>
                <strong>
                  {order.paymentMethod === "VNPAY"
                    ? "VNPay · Thanh toán trực tuyến"
                    : "Thanh toán khi nhận hàng (COD)"}
                </strong>
                <span
                  className={
                    "badge " + (payment?.status === "PAID" ? "green" : "")
                  }
                >
                  {payment
                    ? paymentLabels[payment.status] || payment.status
                    : "Chưa khởi tạo thanh toán"}
                </span>
                {paymentError && (
                  <Alert>
                    {paymentError}
                    <button className="text-button" onClick={() => void load()}>
                      Kiểm tra lại
                    </button>
                  </Alert>
                )}
                {!staff &&
                  order.status === "PENDING_PAYMENT" &&
                  payment?.status !== "PAID" &&
                  payment?.status !== "REQUIRES_RECONCILIATION" && (
                    <button
                      className="button primary"
                      disabled={busy}
                      onClick={() => void pay()}
                    >
                      {busy ? "Đang mở thanh toán..." : "Thanh toán VNPay"}
                    </button>
                  )}
                {!staff &&
                  !payment &&
                  order.paymentMethod === "COD" &&
                  !["CANCELLED", "RETURNED"].includes(order.status) && (
                    <button
                      className="button"
                      disabled={busy}
                      onClick={() => void initializeCod()}
                    >
                      Khởi tạo thanh toán COD
                    </button>
                  )}
                {staff &&
                  order.status === "DELIVERED" &&
                  order.paymentMethod === "COD" &&
                  payment?.status !== "PAID" && (
                    <button
                      className="button primary"
                      disabled={busy}
                      onClick={() => setCommand("COD")}
                    >
                      Xác nhận đã thu COD
                    </button>
                  )}
                {payment?.status === "REQUIRES_RECONCILIATION" && (
                  <Alert error={false}>
                    Giao dịch đã được ghi nhận. Cần liên hệ cửa hàng để đối soát
                    trước khi thực hiện thêm.
                  </Alert>
                )}
              </section>
              {!staff && (
                <Totals {...order} couponCode={order.couponCodeSnapshot}>
                  {canCancel && (
                    <>
                      <button
                        className="button danger"
                        disabled={busy}
                        onClick={() => setCommand("CANCELLED")}
                      >
                        Hủy đơn hàng
                      </button>
                      <small className="muted">
                        Có thể hủy khi đơn đang chờ thanh toán hoặc chờ xác
                        nhận.
                      </small>
                    </>
                  )}
                </Totals>
              )}
              {staff && (
                <section className="panel stack">
                  <h3>Xử lý đơn hàng</h3>
                  {(order.allowedTransitions || []).map((status) => (
                    <button
                      className={
                        "button " +
                        (["CANCELLED", "RETURNED", "DELIVERY_FAILED"].includes(
                          status,
                        )
                          ? "danger"
                          : "primary")
                      }
                      disabled={busy}
                      key={status}
                      onClick={() => setCommand(status)}
                    >
                      {actions[status] || statusLabels[status]}
                    </button>
                  ))}
                  {!order.allowedTransitions?.length && (
                    <p className="muted">
                      Không có thao tác chuyển trạng thái khả dụng.
                    </p>
                  )}
                  {order.status === "DELIVERY_FAILED" && (
                    <p className="muted">
                      Đã giao thất bại{" "}
                      {
                        order.statusHistories.filter(
                          (h) => h.toStatus === "DELIVERY_FAILED",
                        ).length
                      }
                      /3 lần. Chỉ hủy sau khi nhận hàng về kho.
                    </p>
                  )}
                </section>
              )}
            </div>
          </div>
          <ConfirmDialog
            open={command !== null}
            busy={busy}
            title={
              command === "COD"
                ? "Xác nhận đã thu COD?"
                : actions[command as OrderStatus] || "Xác nhận thao tác"
            }
            confirmLabel="Xác nhận"
            onClose={() => setCommand(null)}
            onConfirm={() => void confirm()}
          >
            {command === "COD"
              ? "Chỉ xác nhận khi đã nhận đủ số tiền của đơn hàng."
              : command === "CANCELLED"
                ? order.status === "DELIVERY_FAILED"
                  ? "Xác nhận hàng đã về kho. Đơn sẽ được hủy và hoàn tồn kho, lượt dùng ưu đãi."
                  : "Đơn sẽ được hủy và hoàn tồn kho, lượt dùng ưu đãi. Thao tác không thể hoàn tác."
                : command === "RETURNED"
                  ? "Xác nhận đã nhận và kiểm tra hàng trả trước khi hoàn tồn kho."
                  : "Cập nhật đơn sang trạng thái " +
                    statusLabels[command as OrderStatus] +
                    "."}
          </ConfirmDialog>
        </>
      ) : (
        <div className="empty">
          <Link className="button" to={staff ? "/staff/orders" : "/orders"}>
            Quay lại đơn hàng
          </Link>
        </div>
      )}
    </Layout>
  );
}
