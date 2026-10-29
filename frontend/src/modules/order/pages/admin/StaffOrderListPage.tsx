import { Link } from "react-router-dom";
import { StaffLayout } from "../../components/CommerceLayout";
import {
  Alert,
  EmptyOrders,
  Loading,
  Pagination,
  StatusBadge,
  dateTime,
  money,
  statusLabels,
} from "../../components/OrderUI";
import { useOrderList } from "../../components/useOrderList";
export function StaffOrderListPage() {
  const list = useOrderList(true);
  const orders = list.data?.content || [];
  const stats = [
    [
      "Chờ xác nhận",
      orders.filter((o) => o.status === "WAITING_CONFIRMATION").length,
    ],
    ["Đang đóng gói", orders.filter((o) => o.status === "PACKING").length],
    ["Đang giao", orders.filter((o) => o.status === "SHIPPING").length],
    [
      "Giao thất bại",
      orders.filter((o) => o.status === "DELIVERY_FAILED").length,
    ],
  ];
  return (
    <StaffLayout>
      <div className="page-heading">
        <div>
          <p className="eyebrow">VẬN HÀNH / ĐƠN HÀNG</p>
          <h1>Mỗi đơn hàng, một trải nghiệm tốt.</h1>
          <p>Theo dõi và xử lý đơn hàng của cửa hàng.</p>
        </div>
      </div>
      <div className="stats">
        {stats.map(([label, count]) => (
          <div className="panel stat" key={label}>
            <span className="muted">{label}</span>
            <strong>{count}</strong>
            <small className="green">Trong trang hiện tại</small>
          </div>
        ))}
      </div>
      <form className="filters" onSubmit={list.submit}>
        <input
          aria-label="Tìm mã đơn, tên khách hàng"
          placeholder="Tìm mã đơn, tên khách hàng..."
          value={list.draft}
          onChange={(e) => list.setDraft(e.target.value)}
        />
        <select
          aria-label="Trạng thái đơn hàng"
          value={list.status}
          onChange={(e) => list.changeStatus(e.target.value)}
        >
          <option value="">Tất cả trạng thái</option>
          {Object.entries(statusLabels)
            .filter(([value]) => value !== "CREATED")
            .map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
        </select>
        <button className="button">Tìm kiếm</button>
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
        (orders.length ? (
          <>
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>MÃ ĐƠN</th>
                    <th>KHÁCH HÀNG</th>
                    <th>THỜI GIAN</th>
                    <th>TỔNG TIỀN</th>
                    <th>TRẠNG THÁI</th>
                    <th>THAO TÁC</th>
                  </tr>
                </thead>
                <tbody>
                  {orders.map((o) => (
                    <tr key={o.id}>
                      <td>
                        <strong>#{o.orderCode}</strong>
                      </td>
                      <td>{o.recipientName}</td>
                      <td>{dateTime(o.createdAt)}</td>
                      <td>{money(o.totalAmount)}</td>
                      <td>
                        <StatusBadge status={o.status} />
                      </td>
                      <td>
                        <Link className="orange" to={"/staff/orders/" + o.id}>
                          Xử lý →
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
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
    </StaffLayout>
  );
}
