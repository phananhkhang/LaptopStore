import { Route, Routes } from 'react-router-dom';
import ProductListPage from '../pages/user/ProductListPage';
import ProductDetailPage from '../pages/user/ProductDetailPage';
import AdminProductPage from '../pages/admin/AdminProductPage';

export const CatalogRoutes = () => {
    return (
        <Routes>
            {/* Routes cho USER */}
            <Route path="/products" element={<ProductListPage />} />
            <Route path="/products/:id" element={<ProductDetailPage />} />

            {/* Routes cho ADMIN */}
            <Route path="/admin/products" element={<AdminProductPage />} />
        </Routes>
    );
};