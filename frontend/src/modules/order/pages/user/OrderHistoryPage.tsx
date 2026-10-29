import { Link } from "react-router-dom";
import { useAuth } from "@/app/providers/AuthProvider";
import { StoreLayout } from "../../components/CommerceLayout";
import {
  Alert,
  EmptyOrders,
  Loading,
  OrderItems,
  Pagination,
  StatusBadge,
  dateTime,
  money,
} from "../../components/OrderUI";
import { useOrderList } from "../../components/useOrderList";
export function OrderHistoryPage() {
  const { user, logout } = useAuth();
  const list = useOrderList();
  const tabs = [
    ["", "Tất cả"],
    ["PENDING_PAYMENT", "Chờ thanh toán"],
    ["WAITING_CONFIRMATION", "Chờ xác nhận"],
    ["PACKING", "Đang đóng gói"],
    ["SHIPPING", "Đang giao"],
    ["DELIVERED", "Đã giao"],
    ["CANCELLED", "Đã hủy"],
  ];
  return (
    <StoreLayout>
      <div className="page-heading">
        <div>
          <p className="eyebrow">TÀI KHOẢN CỦA TÔI</p>
          <h1>Đơn hàng của bạn</h1>
          <p>Theo dõi từng chặng đường của chiếc laptop mới.</p>
        </div>
      </div>
      <div className="account-grid">
        <aside className="panel account-nav">
          <span className="avatar">
            {(user?.fullName || "KH").slice(0, 2).toUpperCase()}
          </span>
          <strong>{user?.fullName || "Khách hàng"}</strong>
          <p className="muted">{user?.email}</p>
          <hr className="divider" />
          <Link to="/profile">Hồ sơ</Link>
          <Link to="/me/addresses">Sổ địa chỉ</Link>
          <Link className="active" to="/orders">
            Đơn hàng
          </Link>
          <Link to="/ai-assistant">Tư vấn AI</Link>
          <button className="text-button" onClick={() => void logout()}>
            Đăng xuất
          </button>
        </aside>
        <div className="stack">
          <div className="tabs" aria-label="Lọc đơn theo trạng thái">
            {tabs.map(([value, label]) => (
              <button
                key={value}
                className={list.status === value ? "active" : ""}
                aria-pressed={list.status === value}
                onClick={() => list.changeStatus(value)}
              >
                {label}
              </button>
            ))}
          </div>
          <form className="filters" onSubmit={list.submit}>
            <input
              aria-label="Tìm đơn hàng"
              placeholder="Nhập mã đơn hàng..."
              value={list.draft}
              onChange={(e) => list.setDraft(e.target.value)}
            />
            <button className="button">Tìm đơn hàng</button>
          </form>
          {list.error && (
            <Alert>
              {list.error}
              <button className="text-button" onClick={list.retry}>
                Thử lại
              </button>
            </Alert>
          )}
          {list.loading ? (
            <Loading />
          ) : (
            list.data &&
            (list.data.content.length ? (
              <>
                {list.data.content.map((order) => (
                  <article className="panel order-card" key={order.id}>
                    <header>
                      <strong>{order.orderCode}</strong>
                      <small className="muted">
                        {dateTime(order.createdAt)}
                      </small>
                      <StatusBadge status={order.status} />
                    </header>
                    <OrderItems items={order.items || []} />
                    <strong className="total">
                      {money(order.totalAmount)}
                    </strong>
                    <footer>
                      <small className="muted">
                        {order.status === "DELIVERED"
                          ? "Cảm ơn bạn đã tin tưởng LaptopStore."
                          : "Mọi thông tin đơn hàng sẽ được cập nhật tại đây."}
                      </small>
                      <Link className="button" to={"/orders/" + order.id}>
                        Xem chi tiết
                      </Link>
                    </footer>
                  </article>
                ))}
                <Pagination
                  page={list.page}
                  total={list.data.totalElements}
                  totalPages={list.data.totalPages}
                  onPage={list.changePage}
                />
              </>
            ) : (
              <EmptyOrders />
            ))
          )}
        </div>
      </div>
    </StoreLayout>
  );
}
