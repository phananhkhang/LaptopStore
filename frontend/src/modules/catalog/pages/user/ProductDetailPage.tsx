import { resolveImageUrl } from '@/utils/image';
import React, { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { catalogApi } from '../../api/catalogApi';
import { ProductResponse, VariantSnapshot } from '../../types/catalog';

const ProductDetailPage: React.FC = () => {
    const { productId } = useParams<{ productId: string }>();
    const [product, setProduct] = useState<ProductResponse | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    // State lưu cấu hình (variant) đang được chọn và ảnh hiển thị
    const [selectedVariant, setSelectedVariant] = useState<VariantSnapshot | null>(null);
    const [mainImage, setMainImage] = useState<string>('');

    useEffect(() => {
        if (productId) {
            fetchProductDetail(Number(productId));
        }
    }, [productId]);

    const fetchProductDetail = async (id: number) => {
        try {
            setLoading(true);
            setError(null);
            const data = await catalogApi.getProductById(id);
            setProduct(data);

            if (data.variants && data.variants.length > 0) {
                setSelectedVariant(data.variants[0]);
            }

            if (data.images && data.images.length > 0) {
                const primaryImg = data.images.find(img => img.isPrimary);
                setMainImage(primaryImg ? primaryImg.imageUrl : data.images[0].imageUrl);
            }
        } catch (err) {
            console.error('Lỗi tải chi tiết sản phẩm:', err);
            setError('Không thể tải thông tin sản phẩm. Sản phẩm có thể không tồn tại hoặc đã bị xóa.');
        } finally {
            setLoading(false);
        }
    };

    const handleAddToCart = () => {
        if (!product || !selectedVariant || selectedVariant.quantity === 0) return;

        const cartKey = 'laptopstore_cart';
        const existingCart = JSON.parse(localStorage.getItem(cartKey) || '[]');
        const existingItemIndex = existingCart.findIndex((item: any) => item.variantId === selectedVariant.id);

        if (existingItemIndex >= 0) {
            existingCart[existingItemIndex].quantity += 1;
        } else {
            existingCart.push({
                productId: product.id,
                productName: product.name,
                variantId: selectedVariant.id,
                sku: selectedVariant.sku,
                ram: selectedVariant.ram,
                storage: selectedVariant.storage,
                color: selectedVariant.color,
                price: selectedVariant.price,
                quantity: 1,
                thumbnail: mainImage
            });
        }

        localStorage.setItem(cartKey, JSON.stringify(existingCart));
        alert('Đã thêm sản phẩm vào giỏ hàng thành công!');
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center min-h-[600px]">
                <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary"></div>
            </div>
        );
    }

    if (error || !product) {
        return (
            <div className="max-w-[1280px] mx-auto p-4 mt-10 text-center">
                <p className="text-red-500 mb-4">{error}</p>
                <Link to="/products" className="text-primary hover:underline">← Quay lại danh sách sản phẩm</Link>
            </div>
        );
    }

    return (
        <div className="flex flex-col items-center py-7 pb-12 gap-6 w-full max-w-[1280px] mx-auto font-sans px-4 xl:px-0">
            
            {/* Breadcrumb */}
            <div className="w-full flex items-center gap-2 text-[12px] mb-2">
                <Link to="/" className="text-ink-lighter font-semibold hover:text-primary">Trang chủ</Link>
                <span className="text-ink-lighter">/</span>
                <Link to="/products" className="text-ink-lighter font-semibold hover:text-primary">{product.categoryName || 'Laptop'}</Link>
                <span className="text-ink-lighter">/</span>
                <span className="text-ink font-medium">{product.name}</span>
            </div>

            {/* Product detail */}
            <div className="w-full flex flex-col lg:flex-row gap-12">
                
                {/* Left: Product gallery (600px) */}
                <div className="w-full lg:w-[600px] flex flex-col gap-4">
                    {/* Main visual */}
                    <div className="w-full h-[390px] bg-white rounded-[24px] flex justify-center items-center overflow-hidden border border-surface-border">
                        {mainImage ? (
                            <img src={resolveImageUrl(mainImage)} alt={product.name} className="max-h-[300px] object-contain mix-blend-multiply" />
                        ) : (
                            <span className="text-ink-lighter">Chưa có hình ảnh</span>
                        )}
                    </div>
                    
                    {/* Thumbnails */}
                    {product.images && product.images.length > 0 && (
                        <div className="flex gap-4 overflow-x-auto pb-2 scrollbar-hide h-[80px]">
                            {product.images.map(img => (
                                <div
                                    key={img.id}
                                    onClick={() => setMainImage(img.imageUrl)}
                                    className={`w-[116px] h-full bg-white rounded-xl border flex-shrink-0 cursor-pointer flex items-center justify-center p-2 transition-colors
                                        ${mainImage === img.imageUrl ? 'border-primary ring-1 ring-primary' : 'border-surface-border hover:border-primary/50'}`}
                                >
                                    <img src={resolveImageUrl(img.imageUrl)} alt="Thumbnail" className="max-h-full object-contain mix-blend-multiply" />
                                </div>
                            ))}
                        </div>
                    )}

                    {/* Delivery and warranty */}
                    <div className="flex items-center gap-6 mt-2">
                        <div className="flex items-center gap-2">
                            <div className="w-[22px] h-[22px] rounded-full border-[1.8px] border-primary text-primary flex items-center justify-center">
                                <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                            </div>
                            <span className="text-ink text-[12px] font-semibold">Bảo hành chính hãng</span>
                        </div>
                        <div className="flex items-center gap-2">
                            <div className="w-[22px] h-[22px] rounded-full border-[1.8px] border-primary text-primary flex items-center justify-center">
                                <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" /></svg>
                            </div>
                            <span className="text-ink text-[12px] font-semibold">Giao hàng miễn phí</span>
                        </div>
                    </div>
                </div>

                {/* Right: Product purchase panel (632px) */}
                <div className="w-full lg:flex-1 flex flex-col gap-4">
                    {/* Badge */}
                    <span className="text-primary text-[11px] font-semibold uppercase tracking-wider bg-[#FFF2E9] px-3 py-1.5 rounded-lg w-fit">
                        {product.categoryName || 'LAPTOP'} / {product.brandName}
                    </span>
                    
                    <h1 className="text-ink text-[30px] font-semibold leading-tight">{product.name}</h1>
                    
                    <span className="text-ink-lighter text-[12px]">★ 4.9 · 28 đánh giá · Đã bán 120</span>
                    
                    <p className="text-ink-lighter text-[14px] leading-relaxed line-clamp-2">
                        {product.description || 'Sức mạnh sẵn sàng cho mọi thử thách. Hiệu năng mạnh mẽ cho lập trình, sáng tạo và những trận game mượt mà.'}
                    </p>

                    {/* Variant options */}
                    {product.variants && product.variants.length > 0 ? (
                        <div className="flex flex-col gap-3 mt-2">
                            <h3 className="text-ink text-[13px] font-semibold">Chọn cấu hình</h3>
                            <div className="flex flex-wrap gap-3">
                                {product.variants.map((v) => (
                                    <button
                                        key={v.id}
                                        onClick={() => setSelectedVariant(v)}
                                        className={`h-[48px] min-w-[220px] px-4 rounded-xl text-[13px] font-semibold transition-colors
                                            ${selectedVariant?.id === v.id
                                                ? 'bg-primary text-white' 
                                                : 'bg-white border border-surface-border text-ink hover:border-primary/50'}`}
                                    >
                                        {v.ram} / {v.storage}
                                    </button>
                                ))}
                            </div>
                            <span className="text-ink-lighter text-[12px] mt-1">
                                {selectedVariant?.ram} · {selectedVariant?.storage} · {selectedVariant?.color} · SKU {selectedVariant?.sku}
                            </span>
                        </div>
                    ) : (
                        <div className="p-3 bg-yellow-50 text-yellow-700 rounded-lg text-sm">Chưa có cấu hình.</div>
                    )}

                    {/* Price and Stock */}
                    <div className="flex flex-col gap-2 mt-4">
                        <span className="text-primary text-[36px] font-semibold leading-none">
                            {selectedVariant?.price.toLocaleString('vi-VN')}₫
                        </span>
                        
                        {selectedVariant?.quantity && selectedVariant.quantity > 0 ? (
                            <span className="bg-[#EAF7F0] text-[#16835B] px-3 py-1.5 rounded-lg text-[11px] font-semibold w-fit">
                                Còn hàng · Sẵn sàng giao
                            </span>
                        ) : (
                            <span className="bg-red-50 text-red-600 px-3 py-1.5 rounded-lg text-[11px] font-semibold w-fit">
                                Tạm hết hàng
                            </span>
                        )}
                    </div>

                    {/* Purchase actions */}
                    <div className="flex flex-col sm:flex-row gap-4 mt-2">
                        <button
                            disabled={!selectedVariant || selectedVariant.quantity === 0}
                            className="flex-1 h-[48px] bg-primary text-white rounded-xl text-[13px] font-semibold hover:bg-primary-dark transition-colors disabled:opacity-50"
                        >
                            Mua ngay
                        </button>
                        <button
                            onClick={handleAddToCart}
                            disabled={!selectedVariant || selectedVariant.quantity === 0}
                            className="flex-1 h-[48px] bg-white border border-surface-border text-ink rounded-xl text-[13px] font-semibold hover:bg-gray-50 hover:border-primary transition-colors disabled:opacity-50"
                        >
                            Thêm vào giỏ
                        </button>
                    </div>

                    {/* Coupon offer */}
                    <div className="w-full bg-[#FFF2E9] rounded-xl p-4 flex items-center gap-3 mt-2">
                        <svg className="w-[22px] h-[22px] text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.8} d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" /></svg>
                        <span className="text-primary text-[12px] font-semibold">LAPTOP500 · Giảm 500.000₫ cho đơn từ 20 triệu</span>
                    </div>
                </div>
            </div>

            {/* Product information (Bottom section) */}
            <div className="w-full flex flex-col lg:flex-row gap-6 mt-6">
                
                {/* Specs Card (744px) */}
                <div className="w-full lg:w-[744px] bg-white border border-surface-border rounded-[16px] p-6 flex flex-col gap-4">
                    <h2 className="text-ink text-[20px] font-semibold mb-2">Hiểu rõ chiếc máy của bạn</h2>
                    {product.specification ? (
                        <div className="flex flex-col gap-4">
                            <div className="flex gap-4">
                                <span className="w-[180px] text-ink-lighter text-[12px]">Vi xử lý</span>
                                <span className="flex-1 text-ink text-[12px] font-semibold">{product.specification.cpu || 'Đang cập nhật'}</span>
                            </div>
                            <div className="flex gap-4">
                                <span className="w-[180px] text-ink-lighter text-[12px]">Đồ họa</span>
                                <span className="flex-1 text-ink text-[12px] font-semibold">{product.specification.gpu || 'Đang cập nhật'}</span>
                            </div>
                            <div className="flex gap-4">
                                <span className="w-[180px] text-ink-lighter text-[12px]">Màn hình</span>
                                <span className="flex-1 text-ink text-[12px] font-semibold">{product.specification.screenSize} · {product.specification.screenResolution} · {product.specification.refreshRate}</span>
                            </div>
                            <div className="flex gap-4">
                                <span className="w-[180px] text-ink-lighter text-[12px]">Bộ nhớ</span>
                                <span className="flex-1 text-ink text-[12px] font-semibold">RAM {selectedVariant?.ram || '...'} · SSD {selectedVariant?.storage || '...'}</span>
                            </div>
                        </div>
                    ) : (
                        <p className="text-ink-lighter text-[12px] italic">Đang cập nhật thông số kỹ thuật...</p>
                    )}
                </div>

                {/* Customer review Card (512px) */}
                <div className="w-full lg:flex-1 bg-white border border-surface-border rounded-[16px] p-6 flex flex-col gap-4">
                    <h2 className="text-ink text-[20px] font-semibold">Được yêu thích bởi người dùng</h2>
                    <div className="text-primary text-[24px] tracking-widest leading-none">★★★★★</div>
                    <p className="text-ink-lighter text-[14px] leading-relaxed">
                        "Máy chạy mượt, đóng gói cẩn thận. Rất phù hợp cho công việc và chơi game."
                    </p>
                    <Link to={`/products/${product.id}/reviews`} className="text-ink-lighter text-[12px] font-semibold hover:text-primary mt-2">
                        Xem đánh giá
                    </Link>
                </div>

            </div>
        </div>
    );
};
export default ProductDetailPage;