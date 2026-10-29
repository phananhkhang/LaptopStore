import { FormEvent, ReactNode, useState } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "@/app/providers/AuthProvider";
import "./commerce.css";

export const Icon = ({ name }: { name: string }) => (
  <span className={`commerce-icon commerce-icon-${name}`} aria-hidden="true">
    <img src={`/commerce/${name}.svg`} alt="" />
  </span>
);
export const Brand = ({
  shop = false,
  staff = false,
}: {
  shop?: boolean;
  staff?: boolean;
}) => (
  <Link className="commerce-brand" to="/products">
    <span className={shop ? "commerce-mark" : ""}>
      <Icon name={shop ? "brand" : staff ? "staff-laptop" : "laptop"} />
    </span>
    laptopstore
  </Link>
);

export function StoreLayout({ children }: { children: ReactNode }) {
  const [search, setSearch] = useState("");
  const navigate = useNavigate();
  const submit = (e: FormEvent) => {
    e.preventDefault();
    navigate(`/products?keyword=${encodeURIComponent(search.trim())}`);
  };
  return (
    <div className="commerce store">
      <div className="commerce-utility">
        <div>
          <span>Công nghệ tốt hơn. Mỗi ngày.</span>
          <span>
            Giao hàng toàn quốc <b>Hotline 1800 2026</b>
          </span>
        </div>
      </div>
      <header className="commerce-header">
        <Brand shop />
        <nav aria-label="Cửa hàng">
          <Link to="/products">Laptop</Link>
          <Link to="/orders">Đơn hàng</Link>
          <Link to="/ai-assistant">Tư vấn AI</Link>
        </nav>
        <form className="commerce-search" onSubmit={submit}>
          <Icon name="search" />
          <input
            aria-label="Tìm laptop"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Tìm chiếc laptop của bạn..."
          />
        </form>
        <Link className="icon-link" to="/profile" aria-label="Tài khoản">
          <Icon name="user" />
        </Link>
        <Link className="icon-link" to="/cart" aria-label="Giỏ hàng">
          <Icon name="cart" />
        </Link>
      </header>
      <main className="commerce-main">{children}</main>
      <footer className="commerce-footer">
        <div className="footer-columns">
          <div>
            <Brand />
            <p>
              Cùng bạn tìm chiếc laptop phù hợp, để mỗi ngày có thêm một ý tưởng
              mới.
            </p>
            <span className="trust">
              <Icon name="footer-shield" />
              Chính hãng · Tư vấn tận tâm
            </span>
          </div>
          <div>
            <h3>Khám phá LaptopStore</h3>
            <Link to="/products">Tất cả laptop</Link>
            <Link to="/ai-assistant">Tư vấn cùng AI</Link>
          </div>
          <div>
            <h3>Chăm sóc khách hàng</h3>
            <Link to="/orders">Đơn hàng của tôi</Link>
            <Link to="/profile">Thông tin tài khoản</Link>
            <Link to="/me/addresses">Sổ địa chỉ</Link>
          </div>
          <div>
            <h3>Thanh toán an tâm</h3>
            <p>COD · VNPay</p>
            <p>
              Giao hàng toàn quốc
              <br />
              Hỗ trợ từ 8:00 đến 21:00
              <br />
              Hotline 1800 2026
            </p>
          </div>
        </div>
        <div className="footer-copyright">
          © 2026 LaptopStore. Công nghệ cho điều bạn muốn.
        </div>
      </footer>
    </div>
  );
}

export function StaffLayout({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const links = [
    ["grid", "Tổng quan", "/staff"],
    ["orders", "Đơn hàng", "/staff/orders"],
    ["box", "Kho hàng", "/admin/inventory"],
    ["truck", "Nhập hàng", "/staff/purchase-orders"],
    ["truck", "Nhà cung cấp", "/staff/suppliers"],
  ];
  return (
    <div className="commerce staff">
      <aside className="staff-sidebar">
        <Brand staff />
        <p className="eyebrow muted">VẬN HÀNH CỬA HÀNG</p>
        <nav aria-label="Vận hành">
          {links.map(([icon, label, to]) => (
            <NavLink key={label} to={to} end={to === "/staff"}>
              <Icon name={icon} />
              {label}
            </NavLink>
          ))}
        </nav>
        <div className="staff-help">
          <Icon name="headset" />
          <b>Một ngày thật hiệu quả</b>
          <p>Mọi thao tác, cùng một nơi.</p>
        </div>
        <Link to="/products" className="staff-exit">
          <Icon name="logout" />
          Về cửa hàng
        </Link>
      </aside>
      <div className="staff-workspace">
        <header className="staff-topbar">
          <span>Không gian nhân viên</span>
          <Link to="/profile" className="staff-profile">
            <span className="avatar">
              {(user?.fullName || user?.email || "NV")
                .slice(0, 2)
                .toUpperCase()}
            </span>
            {user?.fullName || user?.email}
          </Link>
        </header>
        <main className="staff-main">{children}</main>
      </div>
    </div>
  );
}
