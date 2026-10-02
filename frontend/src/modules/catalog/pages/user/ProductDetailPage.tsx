import React from 'react';
import { useParams } from 'react-router-dom';

const ProductDetailPage: React.FC = () => {
    // Lấy ID từ trên thanh địa chỉ URL xuống
    const { id } = useParams();

    return (
        <div className="p-4">
            <h1 className="text-2xl font-bold mb-4">Chi tiết sản phẩm</h1>
            <p>Sản phẩm ID: {id}</p>
            <p>Nơi này tuần sau sẽ hiển thị hình ảnh, giá bán và cấu hình chi tiết (Variant).</p>
        </div>
    );
};

export default ProductDetailPage;