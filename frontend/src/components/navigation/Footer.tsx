import { Brand, Icon } from '@/components/ui';
import React from 'react';
import { Link } from 'react-router-dom';

export const Footer: React.FC = () => (
    <footer className="footer">
        <div className="ls-container">
            <div className="footer-cols">
                <div>
                    <Brand />
                    <p>Cùng bạn tìm chiếc laptop phù hợp, để mỗi ngày có thêm một ý tưởng mới.</p>
                    <div className="row" style={{ marginTop: 18, color: 'var(--orange)' }}>
                        <Icon name="shield" />
                        <small>Sản phẩm chính hãng · Tư vấn tận tâm</small>
                    </div>
                </div>
                <div>
                    <h4>Khám phá LaptopStore</h4>
                    <Link to="/products">Tất cả laptop</Link>
                    <Link to="/products?categoryName=Gaming">Laptop gaming</Link>
                    <Link to={`/products?categoryName=${encodeURIComponent('Văn phòng')}`}>Laptop văn phòng</Link>
                    <Link to={`/products?categoryName=${encodeURIComponent('Mỏng nhẹ')}`}>Laptop mỏng nhẹ</Link>
                    <Link to="/ai-assistant">Tư vấn cùng AI</Link>
                </div>
                <div>
                    <h4>Chăm sóc khách hàng</h4>
                    <Link to="/orders">Đơn hàng của tôi</Link>
                    <Link to="/profile">Thông tin tài khoản</Link>
                    <Link to="/me/addresses">Sổ địa chỉ</Link>
                    <a href="tel:18002026">Hotline 1800 2026</a>
                </div>
                <div>
                    <h4>Thanh toán an tâm</h4>
                    <p style={{ marginTop: 0 }}>COD · VNPay</p>
                    <p>Giao hàng toàn quốc<br />Hỗ trợ từ 8:00 đến 21:00</p>
                </div>
            </div>
            <div className="footer-bottom">
                <span>© 2026 LaptopStore. Công nghệ cho điều bạn muốn.</span>
                <span>Đồ án J2EE · Nhóm 4</span>
            </div>
        </div>
    </footer>
);
