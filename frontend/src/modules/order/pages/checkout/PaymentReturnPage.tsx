import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { orderApi } from "../../api/orderApi";
import { Payment, paymentApi } from "../../api/paymentApi";
import { OrderDetail } from "../../types";
import { StoreLayout } from "../../components/CommerceLayout";
import { Alert, Loading, Totals, errorMessage } from "../../components/OrderUI";

export function PaymentReturnPage() {
  const id = sessionStorage.getItem("commerce.paymentOrderId");
  const [payment, setPayment] = useState<Payment>();
  const [order, setOrder] = useState<OrderDetail>();
  const [loading, setLoading] = useState(Boolean(id));
  const [error, setError] = useState("");
  const [reload, setReload] = useState(0);
  useEffect(() => {
    if (!id || !/^\d+$/.test(id)) return;
    let active = true;
    let timer: ReturnType<typeof setTimeout>;
    let attempts = 0;
    setLoading(true);
    setError("");
    async function check() {
      try {
        const [p, o] = await Promise.all([
          paymentApi.getForOrder(Number(id)),
          orderApi.getMineById(id!),
        ]);
        if (!active) return;
        setPayment(p);
        setOrder(o);
        setLoading(false);
        if (p.status === "PENDING" && ++attempts < 5)
          timer = setTimeout(() => void check(), 3000);
      } catch (err) {
        if (active) {
          setError(errorMessage(err, "Chưa tải được kết quả thanh toán."));
          setLoading(false);
        }
      }
    }
    void check();
    return () => {
      active = false;
      clearTimeout(timer);
    };
  }, [id, reload]);
  const paid = payment?.status === "PAID";
  const reconcile = payment?.status === "REQUIRES_RECONCILIATION";
  const failed = payment?.status === "FAILED";
  const cancelled = order?.status === "CANCELLED";
  const title = !id
    ? "Không tìm thấy thông tin thanh toán"
    : reconcile
      ? "Giao dịch cần đối soát"
      : paid
        ? "Thanh toán thành công"
        : cancelled
          ? "Đơn hàng đã hủy hoặc hết thời gian"
          : failed
            ? "Thanh toán chưa hoàn tất"
            : "Đang xác minh thanh toán";
  return (
    <StoreLayout>
      <div className="page-heading">
        <div>
          <h1>{title}</h1>
          <p>Kết quả được cập nhật từ hệ thống thanh toán của cửa hàng.</p>
        </div>
      </div>
      {loading ? (
        <Loading />
      ) : (
        <div className={order ? "columns detail" : ""}>
          <section className="panel payment-result stack">
            {paid ? (
              <img
                src="/commerce/workspace-box.svg"
                width="270"
                height="203"
                alt=""
              />
            ) : failed || cancelled ? (
              <div className="payment-failure-symbol" aria-hidden="true">
                !
              </div>
            ) : (
              <span className="payment-clock">
                <img src="/commerce/clock.svg" alt="" />
              </span>
            )}
            <span className={"badge " + (paid ? "green" : failed ? "red" : "")}>
              {!id
                ? "Chưa có thông tin"
                : paid
                  ? "Đã thanh toán"
                  : reconcile
                    ? "Cần đối soát"
                    : failed
                      ? "Chưa thành công"
                      : cancelled
                        ? "Đã hủy"
                        : "Chờ ngân hàng xác nhận"}
            </span>
            <h2>{title}</h2>
            {error && <Alert>{error}</Alert>}
            <p className="muted">
              {!id
                ? "Mở chi tiết đơn hàng để kiểm tra thanh toán."
                : reconcile
                  ? "Cửa hàng đã ghi nhận giao dịch và sẽ hỗ trợ đối soát. Vui lòng không thanh toán thêm."
                  : paid
                    ? "Cảm ơn bạn. Cửa hàng đã xác nhận khoản thanh toán cho đơn này."
                    : cancelled
                      ? "Đơn đã được hủy. Nếu tài khoản đã bị trừ tiền, hãy liên hệ cửa hàng để đối soát."
                      : failed
                        ? "Đơn hàng vẫn được lưu. Bạn có thể thử lại thanh toán từ chi tiết đơn."
                        : "Kết quả sẽ được cập nhật sau khi có xác nhận từ máy chủ. Chưa cần thanh toán thêm."}
            </p>
            <div className="placed-actions">
              {id && (
                <button
                  className="button primary"
                  onClick={() => setReload((n) => n + 1)}
                >
                  Kiểm tra lại
                </button>
              )}
              <Link className="button" to={id ? "/orders/" + id : "/orders"}>
                {id ? "Xem chi tiết đơn hàng" : "Danh sách đơn hàng"}
              </Link>
            </div>
          </section>
          {order && <Totals {...order} couponCode={order.couponCodeSnapshot} />}
        </div>
      )}
    </StoreLayout>
  );
}
