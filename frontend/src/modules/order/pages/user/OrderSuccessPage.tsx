import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { orderApi } from "../../api/orderApi";
import { OrderDetail } from "../../types";
import { StoreLayout } from "../../components/CommerceLayout";
import { Alert, Loading, money, errorMessage } from "../../components/OrderUI";
export function OrderSuccessPage() {
  const { orderId } = useParams();
  const [order, setOrder] = useState<OrderDetail>();
  const [error, setError] = useState("");
  useEffect(() => {
    let active = true;
    orderApi
      .getMineById(orderId || "")
      .then((data) => {
        if (active) setOrder(data);
      })
      .catch((err) => {
        if (active) setError(errorMessage(err));
      });
    return () => {
      active = false;
    };
  }, [orderId]);
  return (
    <StoreLayout>
      {error ? (
        <Alert>
          {error}
          <Link to="/orders">Xem đơn hàng</Link>
        </Alert>
      ) : !order ? (
        <Loading />
      ) : (
        <section className="panel placed-order">
          <img
            src="/commerce/workspace-box.svg"
            width="270"
            height="203"
            alt=""
          />
          <span className="badge green">ĐƠN HÀNG ĐÃ ĐƯỢC TẠO</span>
          <h1>Chiếc laptop mới sắp đến rồi!</h1>
          <p className="muted">
            Cảm ơn bạn đã lựa chọn LaptopStore.
            <br />
            Theo dõi tiến trình đơn hàng ngay tại đây.
          </p>
          <h3>Đơn hàng #{order.orderCode}</h3>
          <div className="placed-totals">
            <div>
              <p className="muted">Tổng thanh toán</p>
              <strong className="total">{money(order.totalAmount)}</strong>
            </div>
            <div>
              <p className="muted">Phương thức thanh toán</p>
              <strong>
                {order.paymentMethod === "VNPAY"
                  ? "VNPay"
                  : "Thanh toán khi nhận hàng"}
              </strong>
            </div>
          </div>
          <div className="placed-actions">
            <Link className="button primary" to={"/orders/" + order.id}>
              Theo dõi đơn hàng
            </Link>
            <Link className="button" to="/products">
              Tiếp tục mua sắm
            </Link>
          </div>
        </section>
      )}
    </StoreLayout>
  );
}
