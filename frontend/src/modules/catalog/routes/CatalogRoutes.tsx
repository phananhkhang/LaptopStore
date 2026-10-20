import { Route, Routes } from 'react-router-dom';
import ProductListPage from '../pages/user/ProductListPage';
import ProductDetailPage from '../pages/user/ProductDetailPage';
import AdminProductPage from '../pages/admin/AdminProductPage';
import AdminCategoryPage from '../pages/admin/AdminCategoryPage';

export const CatalogRoutes = () => {
    return (
        <Routes>
            {/* Routes cho USER */}
            <Route path="/products" element={<ProductListPage />} />
            <Route path="/products/:id" element={<ProductDetailPage />} />

            {/* Routes cho ADMIN */}
            <Route path="/admin/products" element={<AdminProductPage />} />
            <Route path="/admin/categories" element={<AdminCategoryPage />} />
        </Routes>
    );
};
