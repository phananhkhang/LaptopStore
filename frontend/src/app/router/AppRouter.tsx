import { EmptyState, ToastProvider } from '@/components/ui';
import { ROLES } from '@/constants/roles';
import React, { useEffect } from 'react';
import { BrowserRouter, Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { PublicLayout } from '../../layouts/PublicLayout';
import { StaffLayout } from '../../layouts/StaffLayout';
import { CartProvider } from '../providers/CartProvider';
import { ProtectedRoute } from './ProtectedRoute';
import { RoleBasedRoute } from './RoleBasedRoute';

// Catalog
import AdminCategoryPage from '@/modules/catalog/pages/admin/AdminCategoryPage';
import AdminProductPage from '@/modules/catalog/pages/admin/AdminProductPage';
import { HomeUser } from '@/modules/catalog/pages/user/HomeUser';
import ProductDetailPage from '@/modules/catalog/pages/user/ProductDetailPage';
import ProductListPage from '@/modules/catalog/pages/user/ProductListPage';

// Auth & User
import { LoginPage } from '@/modules/auth/pages/LoginPage';
import { RegisterPage } from '@/modules/auth/pages/RegisterPage';
import { ProfilePage } from '@/modules/user/pages/ProfilePage';

// AI
import { AIChatWidget } from '@/modules/ai/components/AIChatWidget';
import { AIChatPage } from '@/modules/ai/pages/AIChatPage';

// Cart & Order
import { CartPage } from '@/modules/cart/pages/CartPage';
import { StaffOrderListPage } from '@/modules/order/pages/admin/StaffOrderListPage';
import { CheckoutPage } from '@/modules/order/pages/checkout/CheckoutPage';
import { OrderDetailPage } from '@/modules/order/pages/user/OrderDetailPage';
import { OrderHistoryPage } from '@/modules/order/pages/user/OrderHistoryPage';

// Staff / Admin
import { OpsDashboardPage } from '@/modules/dashboard/pages/OpsDashboardPage';
import { InventoryPage } from '@/modules/inventory/pages/admin/InventoryPage';
import PurchaseOrderListPage from '@/modules/procurement/pages/staff/PurchaseOrderListPage';
import SupplierListPage from '@/modules/procurement/pages/staff/SupplierListPage';
import { PromotionRoutes } from '@/modules/promotion/routes/PromotionRoutes';
import { ProductReviewsPage, StaffReviewPage, WriteReviewPage } from '@/modules/review/routes/ReviewRoutes';

const ErrorPage: React.FC<{ kind: 'forbidden' | 'notFound' }> = ({ kind }) => (
  <div className="ls-container section">
    {kind === 'forbidden' ? (
      <EmptyState icon="lock" title="Không gian này cần quyền truy cập." copy="Tài khoản của bạn chưa có quyền vào khu vực này. Hãy quay lại cửa hàng hoặc đăng nhập bằng tài khoản khác." action={{ label: 'Về trang chủ', to: '/' }} />
    ) : (
      <EmptyState icon="search" title="Có vẻ bạn đang đi lạc." copy="Đường dẫn không còn tồn tại. Cùng quay lại khám phá những chiếc laptop phù hợp nhé." action={{ label: 'Về trang chủ', to: '/' }} />
    )}
  </div>
);

/** Cuộn lên đầu khi đổi trang (giữ nguyên khi chỉ đổi query như lọc/phân trang). */
const ScrollToTop: React.FC = () => {
  const { pathname } = useLocation();
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [pathname]);
  return null;
};

/** Widget tư vấn chỉ hiện ở khu mua hàng, không che bảng ở trang Staff/Admin. */
const ChatWidgetGate: React.FC = () => {
  const { pathname } = useLocation();
  if (pathname.startsWith('/staff') || pathname.startsWith('/admin') || pathname.startsWith('/ai-assistant')) return null;
  return <AIChatWidget />;
};

export const AppRouter: React.FC = () => (
  <BrowserRouter>
    <ToastProvider>
      <CartProvider>
        <ScrollToTop />
        <Routes>
          {/* Khu mua hàng: Header + Footer */}
          <Route element={<PublicLayout />}>
            <Route path="/" element={<HomeUser />} />
            <Route path="/products" element={<ProductListPage />} />
            <Route path="/products/:productId" element={<ProductDetailPage />} />
            <Route path="/products/:productId/reviews" element={<ProductReviewsPage />} />
            <Route path="/promotions" element={<Navigate to="/products?sortPriceDirection=asc&offer=1" replace />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/ai-assistant" element={<AIChatPage />} />
            <Route path="/403" element={<ErrorPage kind="forbidden" />} />

            <Route element={<ProtectedRoute />}>
              <Route path="/profile" element={<ProfilePage />} />
              <Route path="/me/addresses" element={<ProfilePage />} />
              <Route path="/cart" element={<CartPage />} />
              <Route path="/checkout" element={<CheckoutPage />} />
              <Route path="/orders" element={<OrderHistoryPage />} />
              <Route path="/orders/:orderId" element={<OrderDetailPage />} />
              <Route path="/me/order-items/:orderItemId/review" element={<WriteReviewPage />} />
            </Route>

            <Route path="*" element={<ErrorPage kind="notFound" />} />
          </Route>

          {/* Staff & Admin */}
          <Route element={<RoleBasedRoute allowedRoles={[ROLES.STAFF, ROLES.ADMIN]} />}>
            <Route element={<StaffLayout />}>
              <Route path="/staff" element={<Navigate to="/staff/dashboard" replace />} />
              <Route path="/staff/dashboard" element={<OpsDashboardPage />} />
              <Route path="/staff/orders" element={<StaffOrderListPage />} />
              <Route path="/staff/inventory" element={<InventoryPage />} />
              <Route path="/staff/procurement" element={<PurchaseOrderListPage />} />
              <Route path="/staff/suppliers" element={<SupplierListPage />} />
              <Route path="/staff/reviews" element={<StaffReviewPage />} />
              <Route path="/staff/*" element={<Navigate to="/staff/dashboard" replace />} />
            </Route>
          </Route>

          {/* Admin only */}
          <Route element={<RoleBasedRoute allowedRoles={[ROLES.ADMIN]} />}>
            <Route element={<StaffLayout />}>
              <Route path="/admin" element={<OpsDashboardPage />} />
              <Route path="/admin/products" element={<AdminProductPage />} />
              <Route path="/admin/categories" element={<AdminCategoryPage />} />
              <Route path="/admin/promotions/*" element={<PromotionRoutes />} />
              <Route path="/admin/*" element={<Navigate to="/admin" replace />} />
            </Route>
          </Route>
        </Routes>
        <ChatWidgetGate />
      </CartProvider>
    </ToastProvider>
  </BrowserRouter>
);
