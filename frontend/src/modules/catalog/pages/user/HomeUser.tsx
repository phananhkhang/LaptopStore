import { EmptyState, Icon, IconName } from '@/components/ui';
import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { catalogApi } from '../../api/catalogApi';
import ProductCard, { ProductCardSkeleton } from '../../components/ProductCard';
import { Category, ProductSummaryResponse } from '../../types/catalog';

const TRUST: [IconName, string, string][] = [
    ['shield', 'Chính hãng 100%', 'Yên tâm chọn lựa'],
    ['truck', 'Giao hàng toàn quốc', 'Nhanh chóng, tận nơi'],
    ['wallet', 'Thanh toán linh hoạt', 'COD hoặc VNPay'],
    ['headset', 'Tư vấn tận tâm', 'Tìm đúng chiếc máy'],
];

const categoryLink = (name: string) => `/products?categoryName=${encodeURIComponent(name)}`;

export const HomeUser: React.FC = () => {
    const [categories, setCategories] = useState<Category[]>([]);
    const [activeCategory, setActiveCategory] = useState<string>('');
    const [products, setProducts] = useState<ProductSummaryResponse[]>([]);
    const [totalProducts, setTotalProducts] = useState<number | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(false);

    useEffect(() => {
        catalogApi.getCategories()
            .then((list) => setCategories(list.filter((c) => c.active !== false)))
            .catch(() => setCategories([]));
    }, []);

    useEffect(() => {
        let cancelled = false;
        setLoading(true);
        setError(false);
        catalogApi.searchProducts({ page: 0, size: 8, categoryName: activeCategory || undefined })
            .then((page) => {
                if (cancelled) return;
                setProducts(page.content);
                if (!activeCategory) setTotalProducts(page.totalElements);
            })
            .catch(() => !cancelled && setError(true))
            .finally(() => !cancelled && setLoading(false));
        return () => { cancelled = true; };
    }, [activeCategory]);

    const chips = [{ name: '', label: 'Tất cả laptop' }, ...categories.slice(0, 5).map((c) => ({ name: c.name, label: c.name }))];

    return (
        <div className="ls-container">
            {/* Hero */}
            <section className="hero">
                <div className="hero-copy">
                    <div className="eyebrow">Mở máy lên. Mở điều mới.</div>
                    <h1>Chiếc laptop cho <span className="hero-line">mọi <em>ý tưởng.</em></span></h1>
                    <p>Học điều mới, tạo điều hay, chinh phục thử thách.<br />Tìm người bạn đồng hành đúng chất bạn.</p>
                    <div className="row">
                        <Link to="/products" className="btn primary">Khám phá laptop <Icon name="arrow-right" /></Link>
                        <Link to="/ai-assistant" className="btn"><Icon name="sparkles" /> Tư vấn cùng AI</Link>
                    </div>
                    <div className="hero-stats">
                        <div><strong>{totalProducts ?? '—'}+</strong><span>mẫu laptop chính hãng</span></div>
                        <div><strong>{categories.length || '—'}</strong><span>nhu cầu sử dụng</span></div>
                        <div><strong>24h</strong><span>giao nhanh nội thành</span></div>
                    </div>
                </div>
                <div className="hero-art">
                    <img src="/images/hero-laptop.svg" alt="Laptop bạc với hình nền cam" />
                    <div className="hero-label">
                        <Icon name="shield" />
                        <div><strong>Chính hãng. An tâm.</strong><br /><span className="muted">Chọn chiếc máy, trao niềm tin.</span></div>
                    </div>
                </div>
            </section>

            <div className="trust-row">
                {TRUST.map(([icon, title, sub]) => (
                    <div className="trust-item" key={title}>
                        <Icon name={icon} />
                        <div><strong>{title}</strong><small>{sub}</small></div>
                    </div>
                ))}
            </div>

            {/* Sản phẩm nổi bật */}
            <section className="section">
                <div className="section-head">
                    <div>
                        <div className="eyebrow">Lựa chọn dành cho bạn</div>
                        <h2>Đúng nhu cầu. Đúng chiếc máy.</h2>
                        <p className="muted">Những cấu hình được yêu thích, sẵn sàng cho ngày mới.</p>
                    </div>
                    <Link to={activeCategory ? categoryLink(activeCategory) : '/products'} className="btn sm">
                        Xem tất cả <Icon name="arrow-right" />
                    </Link>
                </div>
                <div className="chips" role="tablist" aria-label="Lọc theo nhu cầu">
                    {chips.map((c) => (
                        <button
                            key={c.label}
                            type="button"
                            role="tab"
                            aria-selected={activeCategory === c.name}
                            className={`chip ${activeCategory === c.name ? 'active' : ''}`}
                            onClick={() => setActiveCategory(c.name)}
                        >
                            {c.label}
                        </button>
                    ))}
                </div>

                {loading ? (
                    <div className="products-grid">
                        {Array.from({ length: 4 }, (_, i) => <ProductCardSkeleton key={i} />)}
                    </div>
                ) : error ? (
                    <EmptyState icon="alert" title="Chưa tải được sản phẩm" copy="Không kết nối được máy chủ. Hãy kiểm tra backend đang chạy rồi tải lại trang." />
                ) : products.length === 0 ? (
                    <EmptyState icon="search" title="Chưa có sản phẩm trong nhóm này" copy="Thử chọn nhu cầu khác hoặc xem toàn bộ laptop." action={{ label: 'Xem tất cả laptop', to: '/products' }} />
                ) : (
                    <div className="products-grid">
                        {products.map((p) => <ProductCard key={p.id} product={p} />)}
                    </div>
                )}
            </section>

            {/* Bộ sưu tập */}
            <section className="section" style={{ paddingTop: 8 }}>
                <div className="collection-grid">
                    <Link to={categoryLink('Mỏng nhẹ')} className="collection orange">
                        <div>
                            <div className="eyebrow" style={{ fontSize: 9 }}>Work anywhere</div>
                            <h3>Nhẹ hành trang.<br />Rộng cảm hứng.</h3>
                            <p>Không gian làm việc theo bạn đến mọi nơi.</p>
                            <span className="text-btn">Laptop mỏng nhẹ <Icon name="arrow-right" /></span>
                        </div>
                        <img src="/images/product-silver.svg" alt="Laptop mỏng nhẹ màu bạc" />
                    </Link>
                    <Link to={categoryLink('Gaming')} className="collection">
                        <div>
                            <div className="eyebrow" style={{ fontSize: 9 }}>Play your way</div>
                            <h3>Sẵn sàng cho<br />trận đấu lớn.</h3>
                            <p>Cấu hình mạnh. Từng khung hình mượt mà.</p>
                            <span className="text-btn">Khám phá gaming <Icon name="arrow-right" /></span>
                        </div>
                        <img src="/images/product-dark.svg" alt="Laptop gaming màu đen" />
                    </Link>
                </div>
            </section>

            <div className="ai-banner">
                <div className="ai-symbol"><Icon name="sparkles" /></div>
                <div>
                    <h3>Chưa biết chọn máy nào?</h3>
                    <p className="muted">Kể cho AI nghe nhu cầu và ngân sách của bạn.</p>
                </div>
                <Link to="/ai-assistant" className="btn primary push">Tìm máy cùng AI <Icon name="arrow-right" /></Link>
            </div>
        </div>
    );
};

export default HomeUser;
