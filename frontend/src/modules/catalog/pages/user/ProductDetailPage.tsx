import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { ProductResponse, VariantSnapshot } from '../../types';
import { productApi } from '../../api/productApi';

import { cartApi } from '@/modules/cart/api/cartApi';
import { useAuth } from '@/app/providers/AuthProvider';
import { errorMessage } from '@/modules/order/components/OrderUI';

const ProductDetailPage: React.FC = () => {
    const navigate = useNavigate();
    const { isAuthenticated } = useAuth();
    const [adding, setAdding] = useState(false);
    const [cartMessage, setCartMessage] = useState('');
    const { id } = useParams<{ id: string }>();
    const [product, setProduct] = useState<ProductResponse | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    // State lưu cấu hình (variant) đang được người dùng chọn
    const [selectedVariant, setSelectedVariant] = useState<VariantSnapshot | null>(null);
    // State lưu ảnh đang được chọn để hiển thị ở khung to
    const [mainImage, setMainImage] = useState<string>('');

    useEffect(() => {
        if (id) {
            fetchProductDetail(Number(id));
        }
    }, [id]);

    const fetchProductDetail = async (productId: number) => {
        try {
            setLoading(true);
            setError(null);
            const data = await productApi.getProductById(productId);
            setProduct(data);

            // Mặc định chọn variant đầu tiên nếu có
            if (data.variants && data.variants.length > 0) {
                setSelectedVariant(data.variants[0]);
            }

            // Mặc định lấy ảnh primary hoặc ảnh đầu tiên
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

    const handleAddToCart = async () => {
        if (!product || !selectedVariant || selectedVariant.quantity === 0 || adding) return;
        if (!isAuthenticated) { navigate('/login', { state: { from: { pathname: '/products/' + product.id } } }); return; }
        setAdding(true); setCartMessage('');
        try { await cartApi.addItem(selectedVariant.id, 1); setCartMessage('Đã thêm vào giỏ hàng.'); }
        catch (err) { setCartMessage(errorMessage(err)); }
        finally { setAdding(false); }
    };

    if (loading) {return <div className="max-w-7xl mx-auto p-4 mt-10 text-center text-gray-500">Đang tải thông tin sản phẩm...</div>;}

    if (error || !product) {
        return (
            <div className="max-w-7xl mx-auto p-4 mt-10 text-center">
                <p className="text-red-500 mb-4">{error}</p>
                <Link to="/products" className="text-blue-600 hover:underline">← Quay lại danh sách sản phẩm</Link>
            </div>
        );
    }

    return (
        <div className="max-w-7xl mx-auto p-4 mt-6">
            <div className="mb-4">
                <Link to="/products" className="text-blue-600 hover:underline text-sm font-medium">← Trở về danh mục</Link>
            </div>

            <div className="flex flex-col md:flex-row gap-8">
                {/* Cột Trái: Hình ảnh sản phẩm */}
                <div className="w-full md:w-1/2">
                    <div className="bg-gray-100 aspect-video rounded-lg flex items-center justify-center shadow-inner overflow-hidden mb-4">
                        {mainImage ? (
                            <img src={mainImage} alt={product.name} className="h-full object-contain mix-blend-multiply" />
                        ) : (
                            <span className="text-gray-400 font-medium">Chưa có hình ảnh</span>
                        )}
                    </div>
                    {/* Thumbnail gallery */}
                    {product.images && product.images.length > 0 && (
                        <div className="flex gap-2 overflow-x-auto py-2">
                            {product.images.map(img => (
                                <div
                                    key={img.id}
                                    onClick={() => setMainImage(img.imageUrl)}
                                    className={`w-16 h-16 bg-gray-50 rounded border-2 cursor-pointer flex-shrink-0 flex items-center justify-center p-1 
                                        ${mainImage === img.imageUrl ? 'border-blue-600' : 'border-gray-200 hover:border-blue-400'}`}
                                >
                                    <img src={img.imageUrl} alt="Thumbnail" className="max-h-full object-contain" />
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                {/* Cột Phải: Thông tin & Cấu hình (Variants) */}
                <div className="w-full md:w-1/2">
                    <h1 className="text-2xl font-bold mb-2 text-gray-800">{product.name}</h1>
                    <p className="text-gray-600 mb-4 text-sm">
                        Thương hiệu: <span className="font-semibold text-gray-800">{product.brandName}</span> | 
                        Danh mục: <span className="font-semibold text-gray-800">{product.categoryName}</span> |
                        Trạng thái: <span className={`ml-1 font-semibold ${product.status === 'ACTIVE' ? 'text-green-600' : 'text-red-600'}`}>{product.status}</span>
                    </p>

                    {/* Khu vực chọn Variant */}
                    {product.variants && product.variants.length > 0 ? (
                        <div className="mb-6 p-5 bg-white rounded-xl border border-gray-200 shadow-sm">
                            <h3 className="font-semibold mb-3 text-gray-700">Chọn cấu hình:</h3>
                            <div className="flex flex-wrap gap-3">
                                {product.variants.map((v) => (
                                    <button
                                        key={v.id}
                                        onClick={() => setSelectedVariant(v)}
                                        className={`border rounded-lg px-4 py-2 text-sm transition text-left
                                            ${selectedVariant?.id === v.id
                                                ? 'border-blue-600 bg-blue-50 ring-1 ring-blue-600' 
                                                : 'border-gray-300 hover:border-blue-400 bg-white'}`}
                                    >
                                        <div className={`font-semibold ${selectedVariant?.id === v.id ? 'text-blue-700' : 'text-gray-800'}`}>
                                            {v.ram} - {v.storage}
                                        </div>
                                        <div className="text-xs text-gray-500 mt-1">Màu: {v.color}</div>
                                    </button>
                                ))}
                            </div>

                            <div className="mt-6 pt-6 border-t border-gray-100">
                                <p className="text-3xl text-red-600 font-bold mb-1">
                                    {selectedVariant?.price.toLocaleString('vi-VN')} đ
                                </p>
                                <p className="text-sm text-gray-500 mb-6">
                                    Mã SP: <span className="font-mono">{selectedVariant?.sku}</span> | 
                                    Tồn kho: <span className={selectedVariant?.quantity && selectedVariant.quantity > 0 ? 'text-green-600 font-semibold' : 'text-red-600 font-semibold'}>
                                        {selectedVariant?.quantity && selectedVariant.quantity > 0 ? `${selectedVariant.quantity} sản phẩm` : 'Hết hàng'}
                                    </span>
                                </p>
                                <button
                                    disabled={adding || !selectedVariant || selectedVariant.quantity === 0}
                                    onClick={() => void handleAddToCart()}
                                    className="bg-blue-600 text-white px-8 py-3 rounded-lg hover:bg-blue-700 w-full md:w-auto font-medium transition shadow-sm disabled:opacity-50 disabled:cursor-not-allowed"
                                >
                                    {adding ? 'Đang thêm...' : selectedVariant?.quantity && selectedVariant.quantity > 0 ? 'Thêm vào giỏ hàng' : 'Tạm hết hàng'}
                                </button>
                                {cartMessage && <p role="status" className="mt-4">{cartMessage} <Link to="/cart" className="text-orange-700 underline">Xem giỏ hàng</Link></p>}
                            </div>
                        </div>
                    ) : (
                        <div className="p-4 bg-yellow-50 text-yellow-700 rounded-lg border border-yellow-200">
                            Sản phẩm này hiện chưa có phiên bản cấu hình nào.
                        </div>
                    )}
                </div>
            </div>

            {/* Phần dưới: Thông số kỹ thuật & Mô tả */}
            <div className="mt-12 border-t pt-8 grid grid-cols-1 md:grid-cols-2 gap-10">
                {product.specification ? (
                    <div>
                        <h2 className="text-xl font-bold mb-4 text-gray-800">Thông số kỹ thuật</h2>
                        <div className="bg-white border rounded-lg overflow-hidden">
                            <table className="w-full text-sm text-left text-gray-700">
                                <tbody className="divide-y divide-gray-200">
                                    <tr className="hover:bg-gray-50"><th className="py-3 px-4 w-1/3 bg-gray-50 text-gray-600 font-medium">CPU</th><td className="py-3 px-4">{product.specification.cpu || 'Đang cập nhật'}</td></tr>
                                    <tr className="hover:bg-gray-50"><th className="py-3 px-4 w-1/3 bg-gray-50 text-gray-600 font-medium">GPU</th><td className="py-3 px-4">{product.specification.gpu || 'Đang cập nhật'}</td></tr>
                                    <tr className="hover:bg-gray-50"><th className="py-3 px-4 w-1/3 bg-gray-50 text-gray-600 font-medium">Màn hình</th><td className="py-3 px-4">{product.specification.screenSize} ({product.specification.screenResolution}) - {product.specification.refreshRate}</td></tr>
                                    <tr className="hover:bg-gray-50"><th className="py-3 px-4 w-1/3 bg-gray-50 text-gray-600 font-medium">Hệ điều hành</th><td className="py-3 px-4">{product.specification.os || 'Đang cập nhật'}</td></tr>
                                    <tr className="hover:bg-gray-50"><th className="py-3 px-4 w-1/3 bg-gray-50 text-gray-600 font-medium">Pin</th><td className="py-3 px-4">{product.specification.battery || 'Đang cập nhật'}</td></tr>
                                    <tr className="hover:bg-gray-50"><th className="py-3 px-4 w-1/3 bg-gray-50 text-gray-600 font-medium">Khối lượng</th><td className="py-3 px-4">{product.specification.weight || 'Đang cập nhật'}</td></tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                ) : (
                    <div>
                        <h2 className="text-xl font-bold mb-4 text-gray-800">Thông số kỹ thuật</h2>
                        <p className="text-gray-500 italic">Đang cập nhật...</p>
                    </div>
                )}

                <div>
                    <h2 className="text-xl font-bold mb-4 text-gray-800">Mô tả sản phẩm</h2>
                    <div className="prose prose-sm max-w-none text-gray-600">
                        {product.description ? (
                            <p className="leading-relaxed whitespace-pre-wrap">{product.description}</p>
                        ) : (
                            <p className="italic text-gray-400">Chưa có bài viết mô tả cho sản phẩm này.</p>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ProductDetailPage;