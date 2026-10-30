import React from 'react';
import { Link } from 'react-router-dom';

export const Header: React.FC = () => {
    return (
        <header className="w-full bg-surface flex flex-col font-sans border-b border-surface-border sticky top-0 z-50">
            {/* Utility bar */}
            <div className="w-full h-[30px] bg-primary-light flex items-center px-4 md:px-[80px] justify-between">
                <span className="text-primary text-[11px] font-medium">Công nghệ tốt hơn. Mỗi ngày.</span>
                <div className="flex gap-4">
                    <span className="text-ink-lighter text-[11px]">Giao hàng toàn quốc</span>
                    <span className="text-ink-lighter text-[11px] font-semibold">Hotline 1800 2026</span>
                </div>
            </div>

            {/* Main navigation */}
            <div className="w-full h-[86px] bg-surface flex items-center px-4 md:px-[80px] gap-[28px]">
                {/* Brand */}
                <Link to="/" className="flex items-center gap-2">
                    <div className="w-[34px] h-[34px] bg-primary rounded-lg flex justify-center items-center">
                        {/* Icon Laptop giả lập bằng CSS */}
                        <div className="w-[22px] h-[22px] border-[1.8px] border-white rounded-[2px] relative">
                            <div className="absolute bottom-[-6px] left-[-2px] right-[-2px] border-[1.8px] border-white rounded-sm"></div>
                        </div>
                    </div>
                    <span className="text-ink text-[20px] font-semibold">laptopstore</span>
                </Link>

                {/* Shop links */}
                <nav className="hidden md:flex gap-[24px]">
                    <Link to="/products" className="text-primary text-[13px] font-semibold">Laptop</Link>
                    <Link to="/promotions" className="text-ink text-[13px] font-semibold">Khuyến mãi</Link>
                    <Link to="/ai-assistant" className="text-ink text-[13px] font-semibold">Tư vấn AI</Link>
                </nav>

                <div className="flex-1"></div>

                {/* Search */}
                <div className="hidden md:flex items-center bg-surface-gray rounded-xl h-[44px] w-[300px] px-3 gap-3">
                    <svg className="w-5 h-5 text-ink-lighter" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                    </svg>
                    <input type="text" placeholder="Tìm chiếc laptop của bạn..." className="bg-transparent border-none outline-none text-[12px] text-ink-lighter w-full" />
                </div>

                {/* Icons (Tài khoản & Giỏ hàng) */}
                <div className="flex gap-4">
                    <Link to="/login" className="w-[22px] h-[22px] text-ink hover:text-primary">
                        <svg fill="none" stroke="currentColor" strokeWidth="1.8" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path></svg>
                    </Link>
                    <Link to="/cart" className="w-[22px] h-[22px] text-ink hover:text-primary relative">
                        <svg fill="none" stroke="currentColor" strokeWidth="1.8" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
                    </Link>
                </div>
            </div>
        </header>
    );
};