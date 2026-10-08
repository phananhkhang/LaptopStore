import { Link } from 'react-router-dom';
import React from 'react';
import { ProductSummaryResponse } from '../types';

interface Props {
    product: ProductSummaryResponse;
}

const ProductCard: React.FC<Props> = ({ product }) => {
    return (
        <div className="border rounded-lg p-4 shadow-sm hover:shadow-lg transition-shadow bg-white flex flex-col h-full">
            <div className="w-full h-48 overflow-hidden rounded-md bg-gray-100 mb-4 flex items-center justify-center">
                {product.thumbnailImage ? (
                    <img src={product.thumbnailImage} alt={product.name} className="h-full object-contain" />
                ) : (
                    <span className="text-gray-400">No Image</span>
                )}
            </div>
            <h3 className="text-xs text-gray-500 mb-1 uppercase tracking-wide">{product.brandName} • {product.categoryName}</h3>
            <p className="font-semibold text-gray-800 mb-4 flex-grow line-clamp-2" title={product.name}>{product.name}</p>
            
            <div className="flex justify-between items-center mt-auto border-t pt-3">
               <span className={`px-2 py-1 text-xs font-medium rounded-full ${product.status === 'ACTIVE' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                   {product.status}
               </span>
               <Link to={`/products/${product.id}`} className="bg-blue-600 text-white px-3 py-1.5 rounded text-sm hover:bg-blue-700 font-medium transition-colors">
                   Xem chi tiết
               </Link>
            </div>
        </div>
    );
};

export default ProductCard;