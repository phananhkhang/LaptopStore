import { useAuth } from '@/app/providers/AuthProvider';
import { useCart } from '@/app/providers/CartProvider';
import { Brand, Icon, useToast } from '@/components/ui';
import { ROLES } from '@/constants/roles';
import React, { useEffect, useRef, useState } from 'react';
import { Link, NavLink, useLocation, useNavigate, useSearchParams } from 'react-router-dom';

const initials = (name?: string, email?: string) => {
    const source = (name || email || 'LS').trim();
    const parts = source.split(/\s+/).filter(Boolean);
    return (parts.length > 1 ? parts[parts.length - 2][0] + parts[parts.length - 1][0] : source.slice(0, 2)).toUpperCase();
};

export const Header: React.FC = () => {
    const { user, isAuthenticated, logout } = useAuth();
    const { count } = useCart();
    const toast = useToast();
    const navigate = useNavigate();
    const location = useLocation();
    const [searchParams] = useSearchParams();

    const [keyword, setKeyword] = useState(searchParams.get('keyword') ?? '');
    const [menuOpen, setMenuOpen] = useState(false);
    const [mobileOpen, setMobileOpen] = useState(false);
    const menuRef = useRef<HTMLDivElement>(null);

    const isOffer = searchParams.get('offer') === '1';
    const isStaff = !!user?.roles.some((r) => r === ROLES.STAFF || r === ROLES.ADMIN);
    const isAdmin = !!user?.roles.includes(ROLES.ADMIN);

    // Đồng bộ ô tìm kiếm với URL và đóng menu khi chuyển trang.
    useEffect(() => {
        setKeyword(searchParams.get('keyword') ?? '');
    }, [searchParams]);
    useEffect(() => {
        setMenuOpen(false);
        setMobileOpen(false);
    }, [location.pathname, location.search]);

    useEffect(() => {
        if (!menuOpen) return;
        const onClick = (e: MouseEvent) => {
            if (menuRef.current && !menuRef.current.contains(e.target as Node)) setMenuOpen(false);
        };
        const onKey = (e: KeyboardEvent) => e.key === 'Escape' && setMenuOpen(false);
        document.addEventListener('mousedown', onClick);
        document.addEventListener('keydown', onKey);
        return () => {
            document.removeEventListener('mousedown', onClick);
            document.removeEventListener('keydown', onKey);
        };
    }, [menuOpen]);

    const submitSearch = (e: React.FormEvent) => {
        e.preventDefault();
        const q = keyword.trim();
        navigate(q ? `/products?keyword=${encodeURIComponent(q)}` : '/products');
    };

    const handleLogout = async () => {
        setMenuOpen(false);
        try {
            await logout();
        } finally {
            toast('Bạn đã đăng xuất. Hẹn gặp lại!');
            navigate('/');
        }
    };

    const searchForm = (className: string) => (
        <form className={`search ${className}`} onSubmit={submitSearch} role="search">
            <Icon name="search" />
            <input
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                placeholder="Bạn đang tìm laptop nào?"
                aria-label="Tìm sản phẩm"
            />
        </form>
    );

    const navLinks = (
        <>
            <NavLink to="/products" className={({ isActive }) => (isActive && !isOffer ? 'active' : '')}>Laptop</NavLink>
            <Link to="/products?sortPriceDirection=asc&offer=1" className={isOffer ? 'active' : ''}>Khuyến mãi</Link>
            <NavLink to="/ai-assistant" className={({ isActive }) => (isActive ? 'active' : '')}>Tư vấn AI</NavLink>
        </>
    );

    return (
        <>
            <div className="utility">
                <div className="ls-container">
                    <span>Công nghệ tốt hơn. Mỗi ngày.</span>
                    <span className="utility-extra">
                        Giao hàng toàn quốc <span style={{ margin: '0 12px', color: '#d7c3b7' }}>|</span>
                        <a href="tel:18002026">Hotline 1800 2026</a>
                    </span>
                </div>
            </div>
            <header className="shop-header">
                <div className="ls-container header-main">
                    <Brand />
                    <nav className="shop-nav" aria-label="Cửa hàng">{navLinks}</nav>
                    {searchForm('shop-search')}

                    <div className="header-actions">
                        <button
                            type="button"
                            className="icon-btn mobile-only"
                            aria-label={mobileOpen ? 'Đóng menu' : 'Mở menu'}
                            aria-expanded={mobileOpen}
                            onClick={() => setMobileOpen((v) => !v)}
                        >
                            <Icon name={mobileOpen ? 'close' : 'menu'} />
                        </button>

                        {isAuthenticated ? (
                            <div className="account-menu" ref={menuRef}>
                                <button
                                    type="button"
                                    className="icon-btn"
                                    aria-haspopup="menu"
                                    aria-expanded={menuOpen}
                                    aria-label="Tài khoản"
                                    onClick={() => setMenuOpen((v) => !v)}
                                >
                                    <span className="avatar" style={{ width: 30, height: 30, borderRadius: 9, fontSize: 11 }}>
                                        {initials(user?.fullName, user?.email)}
                                    </span>
                                </button>
                                {menuOpen && (
                                    <div className="menu-pop" role="menu">
                                        <div className="menu-head">
                                            <strong style={{ display: 'block', fontSize: 13 }}>{user?.fullName || 'Khách hàng'}</strong>
                                            <small className="muted">{user?.email}</small>
                                        </div>
                                        <Link to="/profile" role="menuitem"><Icon name="user" />Hồ sơ của tôi</Link>
                                        <Link to="/orders" role="menuitem"><Icon name="orders" />Đơn hàng</Link>
                                        <Link to="/me/addresses" role="menuitem"><Icon name="home" />Sổ địa chỉ</Link>
                                        {isStaff && (
                                            <Link to={isAdmin ? '/admin' : '/staff/dashboard'} role="menuitem">
                                                <Icon name="grid" />{isAdmin ? 'Không gian quản trị' : 'Không gian nhân viên'}
                                            </Link>
                                        )}
                                        <button type="button" role="menuitem" onClick={() => void handleLogout()}>
                                            <Icon name="logout" />Đăng xuất
                                        </button>
                                    </div>
                                )}
                            </div>
                        ) : (
                            <Link to="/login" state={{ from: location }} className="icon-btn" aria-label="Đăng nhập">
                                <Icon name="user" />
                            </Link>
                        )}

                        <Link to="/cart" className="icon-btn cart-link" aria-label={`Giỏ hàng${count ? `, ${count} sản phẩm` : ''}`}>
                            <Icon name="cart" />
                            {count > 0 && <span className="cart-count">{count > 99 ? '99+' : count}</span>}
                        </Link>
                    </div>
                </div>

                <div className={`mobile-nav ${mobileOpen ? 'open' : ''}`}>
                    <div className="ls-container">
                        {searchForm('')}
                        {navLinks}
                        {!isAuthenticated && <Link to="/login">Đăng nhập / Đăng ký</Link>}
                    </div>
                </div>
            </header>
        </>
    );
};
