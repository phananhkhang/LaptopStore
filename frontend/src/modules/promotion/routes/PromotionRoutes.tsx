import { Route, Routes } from 'react-router-dom';
import CouponListPage from '../pages/admin/CouponListPage';
import CouponFormPage from '../pages/admin/CouponFormPage';
import CouponUsageHistoryPage from '../pages/admin/CouponUsageHistoryPage';

// Gắn dưới path "/admin/promotions/*" trong AppRouter
export const PromotionRoutes = () => {
    return (
        <Routes>
            <Route index element={<CouponListPage />} />
            <Route path="new" element={<CouponFormPage />} />
            <Route path=":id/edit" element={<CouponFormPage />} />
            <Route path=":id/usages" element={<CouponUsageHistoryPage />} />
        </Routes>
    );
};
