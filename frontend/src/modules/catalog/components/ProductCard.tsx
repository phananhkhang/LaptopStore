import { formatMoney, Icon, ProductImage } from '@/components/ui';
import React from 'react';
import { Link } from 'react-router-dom';
import { ProductSummaryResponse } from '../types/catalog';

interface Props {
    product: ProductSummaryResponse;
}

/** Thẻ sản phẩm theo design (.product-card). */
const ProductCard: React.FC<Props> = ({ product }) => {
    const href = `/products/${product.id}`;
    return (
        <article className="product-card">
            <Link to={href} className="product-visual" aria-label={`Xem ${product.name}`}>
                {product.categoryName && <span className="mini-tag">{product.categoryName}</span>}
                <ProductImage src={product.thumbnailImage} alt={product.name} hint={`${product.categoryName} ${product.name}`} />
            </Link>
            <div className="eyebrow">{product.brandName}</div>
            <h3><Link to={href}>{product.name}</Link></h3>
            <p className="spec">Chính hãng · Bảo hành 12 tháng</p>
            <div className="price">
                {product.startingPrice != null ? (
                    <><small>Giá từ</small>{formatMoney(product.startingPrice)}</>
                ) : (
                    <span className="muted" style={{ fontSize: 14, fontWeight: 600 }}>Liên hệ để báo giá</span>
                )}
            </div>
            <div className="card-foot">
                <span className="rating"><span className="star">✓</span> Miễn phí giao hàng</span>
                <Link to={href} className="btn sm" aria-label={`Xem ${product.name}`}>
                    <Icon name="arrow-right" />
                </Link>
            </div>
        </article>
    );
};

export const ProductCardSkeleton: React.FC = () => (
    <div className="product-card" aria-hidden="true">
        <div className="skeleton" style={{ aspectRatio: '1.48', marginBottom: 16 }} />
        <div className="skeleton" style={{ height: 10, width: '40%', marginBottom: 10 }} />
        <div className="skeleton" style={{ height: 16, width: '90%', marginBottom: 8 }} />
        <div className="skeleton" style={{ height: 16, width: '60%', marginBottom: 18 }} />
        <div className="skeleton" style={{ height: 22, width: '50%' }} />
    </div>
);

export default ProductCard;
