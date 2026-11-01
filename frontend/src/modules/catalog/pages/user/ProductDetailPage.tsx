import { useAuth } from '@/app/providers/AuthProvider';
import { useCart } from '@/app/providers/CartProvider';
import { apiErrorMessage, Badge, EmptyState, formatMoney, Icon, ProductImage, Spinner, useToast } from '@/components/ui';
import { reviewApi } from '@/modules/review/api/reviewApi';
import { Review, ReviewSummary } from '@/modules/review/types';
import React, { useEffect, useMemo, useState } from 'react';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import { catalogApi } from '../../api/catalogApi';
import { ProductResponse, VariantSnapshot } from '../../types/catalog';

const isSellable = (v: VariantSnapshot) => (!v.status || v.status.toUpperCase() === 'ACTIVE') && v.quantity > 0;

const ProductDetailPage: React.FC = () => {
    const { productId } = useParams<{ productId: string }>();
    const { isAuthenticated } = useAuth();
    const { addItem } = useCart();
    const toast = useToast();
    const navigate = useNavigate();
    const location = useLocation();

    const [product, setProduct] = useState<ProductResponse | null>(null);
    const [selectedVariant, setSelectedVariant] = useState<VariantSnapshot | null>(null);
    const [imageIndex, setImageIndex] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [busy, setBusy] = useState<'add' | 'buy' | null>(null);
    const [summary, setSummary] = useState<ReviewSummary | null>(null);
    const [reviews, setReviews] = useState<Review[]>([]);

    useEffect(() => {
        const id = Number(productId);
        if (!Number.isFinite(id)) {
            setError('Đường dẫn sản phẩm không hợp lệ.');
            setLoading(false);
            return;
        }
        let cancelled = false;
        setLoading(true);
        setError(null);
        setImageIndex(0);
        catalogApi.getProductById(id)
            .then((data) => {
                if (cancelled) return;
                setProduct(data);
                const variants = data.variants ?? [];
                setSelectedVariant(variants.find(isSellable) ?? variants[0] ?? null);
                const images = data.images ?? [];
                const primary = images.findIndex((img) => img.isPrimary);
                setImageIndex(primary > 0 ? primary : 0);
            })
            .catch(() => !cancelled && setError('Không thể tải thông tin sản phẩm. Sản phẩm có thể không tồn tại hoặc đã ngừng kinh doanh.'))
            .finally(() => !cancelled && setLoading(false));

        // Đánh giá là phần phụ: lỗi thì chỉ ẩn khối đánh giá.
        reviewApi.getProductReviewSummary(id).then((s) => !cancelled && setSummary(s)).catch(() => setSummary(null));
        reviewApi.getProductReviews(id, 0, 3).then((p) => !cancelled && setReviews(p.content)).catch(() => setReviews([]));
        return () => { cancelled = true; };
    }, [productId]);

    const images = useMemo(() => product?.images ?? [], [product]);
    const currentImage = images[imageIndex]?.imageUrl ?? null;
    const variants = product?.variants ?? [];
    const canBuy = !!selectedVariant && isSellable(selectedVariant);

    const addToCart = async (mode: 'add' | 'buy') => {
        if (!product || !selectedVariant) return;
        if (!isAuthenticated) {
            toast('Đăng nhập để thêm sản phẩm vào giỏ hàng.');
            navigate('/login', { state: { from: location } });
            return;
        }
        try {
            setBusy(mode);
            await addItem(selectedVariant.id, 1);
            if (mode === 'buy') {
                navigate('/checkout');
            } else {
                toast(`Đã thêm ${product.name} vào giỏ hàng.`, { action: { label: 'Xem giỏ', to: '/cart' } });
            }
        } catch (err) {
            toast(apiErrorMessage(err, 'Không thể thêm vào giỏ. Sản phẩm có thể đã hết hàng.'), { tone: 'error' });
        } finally {
            setBusy(null);
        }
    };

    if (loading) return <div className="ls-container section"><Spinner label="Đang tải sản phẩm" /></div>;

    if (error || !product) {
        return (
            <div className="ls-container section">
                <EmptyState icon="search" title="Không tìm thấy sản phẩm" copy={error ?? undefined} action={{ label: 'Quay lại danh sách laptop', to: '/products' }} />
            </div>
        );
    }

    const spec = product.specification;
    const specRows: [string, string | undefined][] = [
        ['Vi xử lý', spec?.cpu],
        ['Đồ họa', spec?.gpu],
        ['Màn hình', [spec?.screenSize, spec?.screenResolution, spec?.refreshRate].filter(Boolean).join(' · ')],
        ['Bộ nhớ', selectedVariant ? `RAM ${selectedVariant.ram} · ${selectedVariant.storage}` : undefined],
        ['Pin', spec?.battery],
        ['Trọng lượng', spec?.weight],
        ['Hệ điều hành', spec?.os],
    ];
    const ratingText = summary && summary.totalReviews > 0
        ? `${summary.averageRating.toFixed(1)} · ${summary.totalReviews} đánh giá`
        : 'Chưa có đánh giá';

    return (
        <div className="ls-container section">
            <div className="breadcrumb">
                <Link to="/">Trang chủ</Link><span>/</span>
                <Link to="/products">Laptop</Link><span>/</span>
                {product.categoryName && <><Link to={`/products?categoryName=${encodeURIComponent(product.categoryName)}`}>{product.categoryName}</Link><span>/</span></>}
                <span>{product.name}</span>
            </div>

            <section className="product-detail">
                <div>
                    <div className="main-product-image">
                        <ProductImage src={currentImage} alt={product.name} hint={`${product.categoryName} ${product.name}`} loading="eager" />
                    </div>
                    {images.length > 1 && (
                        <div className="thumbs">
                            {images.map((img, i) => (
                                <button
                                    key={img.id}
                                    type="button"
                                    className={`thumb ${i === imageIndex ? 'active' : ''}`}
                                    onClick={() => setImageIndex(i)}
                                    aria-label={`Hình sản phẩm ${i + 1}`}
                                >
                                    <ProductImage src={img.imageUrl} alt="" hint={product.name} />
                                </button>
                            ))}
                        </div>
                    )}
                    <div className="row wrap muted small" style={{ marginTop: 22, gap: 24 }}>
                        <span className="row gap8"><Icon name="shield" /> Bảo hành chính hãng</span>
                        <span className="row gap8"><Icon name="truck" /> Giao hàng miễn phí</span>
                        <span className="row gap8"><Icon name="refresh" /> Đổi trả trong 7 ngày</span>
                    </div>
                </div>

                <div className="detail-info">
                    <div className="eyebrow">{product.brandName}{product.categoryName ? ` / ${product.categoryName}` : ''}</div>
                    <h1>{product.name}</h1>
                    <span className="rating"><span className="star">★★★★★</span> {ratingText}</span>
                    {product.description && <p className="muted">{product.description}</p>}

                    {variants.length > 0 ? (
                        <>
                            <strong className="small">Chọn cấu hình</strong>
                            <div className="variant-options">
                                {variants.map((v) => (
                                    <button
                                        key={v.id}
                                        type="button"
                                        className={`variant-option ${selectedVariant?.id === v.id ? 'active' : ''}`}
                                        onClick={() => setSelectedVariant(v)}
                                        aria-pressed={selectedVariant?.id === v.id}
                                    >
                                        <strong>{v.ram} / {v.storage}</strong><br />
                                        <span className="small">{v.color}{!isSellable(v) ? ' · Hết hàng' : ''}</span>
                                    </button>
                                ))}
                            </div>
                            {selectedVariant && <span className="small muted">SKU {selectedVariant.sku} · Màu {selectedVariant.color}</span>}
                            <div className="detail-price">{selectedVariant ? formatMoney(selectedVariant.price) : '—'}</div>
                            {canBuy
                                ? <Badge tone="green">Còn {selectedVariant!.quantity} chiếc · Sẵn sàng giao</Badge>
                                : <Badge tone="red">Tạm hết hàng</Badge>}
                        </>
                    ) : (
                        <div className="notice" style={{ margin: '18px 0' }}>
                            <Icon name="alert" />
                            <div><strong>Sản phẩm đang cập nhật cấu hình</strong><p>Liên hệ hotline 1800 2026 để được tư vấn.</p></div>
                        </div>
                    )}

                    <div className="detail-actions">
                        <button type="button" className="btn primary" disabled={!canBuy || busy !== null} onClick={() => void addToCart('buy')}>
                            {busy === 'buy' ? 'Đang xử lý...' : 'Mua ngay'}
                        </button>
                        <button type="button" className="btn" disabled={!canBuy || busy !== null} onClick={() => void addToCart('add')}>
                            <Icon name="cart" /> {busy === 'add' ? 'Đang thêm...' : 'Thêm vào giỏ'}
                        </button>
                    </div>

                    <div className="notice">
                        <Icon name="tag" />
                        <div>
                            <strong>Có mã giảm giá?</strong>
                            <p>Nhập mã ở bước thanh toán, hệ thống sẽ kiểm tra điều kiện và trừ trực tiếp vào đơn.</p>
                        </div>
                    </div>
                </div>
            </section>

            <section className="section">
                <div className="grid2">
                    <div className="panel">
                        <h3 className="panel-title">Hiểu rõ chiếc máy của bạn</h3>
                        <table className="spec-table">
                            <tbody>
                                {specRows.map(([label, value]) => (
                                    <tr key={label}><td>{label}</td><td>{value || 'Đang cập nhật'}</td></tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                    <div className="panel">
                        <div className="row between">
                            <h3>Trải nghiệm từ người dùng</h3>
                            <span className="rating-large">{summary && summary.totalReviews > 0 ? summary.averageRating.toFixed(1) : '–'}</span>
                        </div>
                        {reviews.length > 0 ? reviews.map((r) => (
                            <div className="review-item" key={r.id}>
                                <div className="row between">
                                    <strong className="small">{r.variantName || 'Khách hàng'}</strong>
                                    <span className="small muted">{new Date(r.createdAt).toLocaleDateString('vi-VN')}</span>
                                </div>
                                <div style={{ color: '#b86710' }}>{'★'.repeat(r.rating)}</div>
                                {r.comment && <p>{r.comment}</p>}
                                <Badge tone="green">Đã mua hàng</Badge>
                            </div>
                        )) : (
                            <div className="review-item">
                                <p className="muted">Chưa có đánh giá công khai cho sản phẩm này. Cảm nhận của bạn sẽ giúp người mua tiếp theo lựa chọn dễ dàng hơn.</p>
                            </div>
                        )}
                        <Link to={`/products/${product.id}/reviews`} className="text-btn small">Xem tất cả đánh giá <Icon name="arrow-right" /></Link>
                    </div>
                </div>
            </section>
        </div>
    );
};

export default ProductDetailPage;
