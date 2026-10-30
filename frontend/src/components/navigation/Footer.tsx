import React from 'react';
import { Link } from 'react-router-dom';

export const Footer: React.FC = () => {
    return (
        <footer className="w-full bg-surface-gray flex flex-col font-sans px-4 md:px-[80px] py-[40px] gap-[28px]">
            {/* Footer columns */}
            <div className="max-w-[1280px] mx-auto w-full flex flex-wrap gap-[40px]">
                {/* Brand promise */}
                <div className="flex flex-col gap-[12px] w-[340px]">
                    <div className="flex items-center gap-[10px]">
                        <div className="w-[28px] h-[28px] border-[1.8px] border-primary rounded text-primary flex items-center justify-center">
                            <span className="text-[10px] font-bold">LS</span>
                        </div>
                        <span className="text-ink text-[20px] font-semibold">laptopstore</span>
                    </div>
                    <p className="text-ink-lighter text-[12px] leading-relaxed">Cùng bạn tìm chiếc laptop phù hợp, để mỗi ngày có một ý tưởng mới.</p>
                    <div className="flex items-center gap-2 text-primary">
                        <svg className="w-[18px] h-[18px]" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.8} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" /></svg>
                        <span className="text-[11px] font-medium">Chính hãng · Tư vấn tận tâm</span>
                    </div>
                </div>

                {/* Khám phá */}
                <div className="flex flex-col gap-[12px] w-[240px]">
                    <span className="text-ink text-[12px] font-semibold">Khám phá LaptopStore</span>
                    <Link to="/products" className="text-ink-lighter text-[12px] hover:text-primary">Tất cả laptop</Link>
                    <Link to="/products?category=gaming" className="text-ink-lighter text-[12px] hover:text-primary">Laptop gaming</Link>
                    <Link to="/products?category=office" className="text-ink-lighter text-[12px] hover:text-primary">Laptop văn phòng</Link>
                    <Link to="/ai-assistant" className="text-ink-lighter text-[12px] hover:text-primary">Tư vấn AI</Link>
                </div>

                {/* Chăm sóc khách hàng */}
                <div className="flex flex-col gap-[12px] w-[240px]">
                    <span className="text-ink text-[12px] font-semibold">Chăm sóc khách hàng</span>
                    <Link to="/orders" className="text-ink-lighter text-[12px] hover:text-primary">Đơn hàng của tôi</Link>
                    <Link to="/profile" className="text-ink-lighter text-[12px] hover:text-primary">Hồ sơ cá nhân</Link>
                    <Link to="/me/addresses" className="text-ink-lighter text-[12px] hover:text-primary">Sổ địa chỉ</Link>
                    <Link to="/login" className="text-ink-lighter text-[12px] hover:text-primary">Đăng nhập</Link>
                </div>

                {/* Payment and delivery */}
                <div className="flex flex-col gap-[12px] w-[280px]">
                    <span className="text-ink text-[12px] font-semibold">Thanh toán an tâm</span>
                    <span className="text-ink-lighter text-[12px]">COD · VNPay</span>
                    <span className="text-ink-lighter text-[12px] leading-relaxed mt-2">
                        Giao hàng toàn quốc<br/>
                        Hỗ trợ từ 8:00 đến 21:00<br/>
                        Hotline: 1800 2026
                    </span>
                </div>
            </div>

            {/* Divider */}
            <div className="w-full max-w-[1280px] mx-auto h-[1px] bg-surface-border"></div>

            {/* Copyright */}
            <div className="w-full max-w-[1280px] mx-auto text-ink-lighter text-[10px]">
                © 2026 LaptopStore. Công nghệ cho điều bạn muốn.
            </div>
        </footer>
    );
};