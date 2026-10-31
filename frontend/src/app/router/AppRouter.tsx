import React from 'react';
import { BrowserRouter, Route, Routes, Navigate } from 'react-router-dom';
import { ProtectedRoute } from './ProtectedRoute';
import { RoleBasedRoute } from './RoleBasedRoute';
import { ROLES } from '@/constants/roles';
import { PublicLayout } from '../../layouts/PublicLayout';
import { HomeUser } from '@/modules/catalog/pages/user/HomeUser';

// Auth & User Pages
import { LoginPage } from '@/modules/auth/pages/LoginPage';
import { RegisterPage } from '@/modules/auth/pages/RegisterPage';
import { ProfilePage } from '@/modules/user/pages/ProfilePage';
import { StaffLayout } from '../../layouts/StaffLayout';

// AI Pages & Widget
import { AIChatPage } from '@/modules/ai/pages/AIChatPage';
import { AIChatWidget } from '@/modules/ai/components/AIChatWidget';

// Cart & Order Pages
import { CartPage } from '@/modules/cart/pages/CartPage';
import { CheckoutPage } from '@/modules/order/pages/checkout/CheckoutPage';
import { StaffOrderListPage } from '@/modules/order/pages/admin/StaffOrderListPage';
import { OrderDetailPage } from '@/modules/order/pages/user/OrderDetailPage';
import { OrderHistoryPage } from '@/modules/order/pages/user/OrderHistoryPage';

// Catalog
import { CatalogRoutes } from '@/modules/catalog/routes/CatalogRoutes';

// Inventory
import { InventoryPage } from '../../modules/inventory/pages/admin/InventoryPage';

// Promotion
import { PromotionRoutes } from '@/modules/promotion/routes/PromotionRoutes';

// Review
import { ProductReviewsPage, StaffReviewPage, WriteReviewPage } from '@/modules/review/routes/ReviewRoutes';
import ProductListPage from '../../modules/catalog/pages/user/ProductListPage';
import ProductDetailPage from '@/modules/catalog/pages/user/ProductDetailPage';

// Placeholders for remaining admin/staff dashboards
const StaffDashboard = () => (
  <div className="max-w-7xl mx-auto p-6">
    <h1 className="text-2xl font-bold text-gray-800">Bàn Làm Việc Nhân Viên (Staff Protected)</h1>
    <p className="text-gray-500 mt-2">Khu vực xử lý đơn hàng và điều phối kho bãi.</p>
  </div>
);

const AdminDashboard = () => (
  <div className="max-w-7xl mx-auto p-6">
    <h1 className="text-2xl font-bold text-gray-800">Trang Quản Trị Hệ Thống (Admin Protected)</h1>
    <p className="text-gray-500 mt-2">Khu vực quản lý cấu hình, người dùng và báo cáo doanh thu toàn hệ thống.</p>
  </div>
);

const ForbiddenPage = () => (
  <div className="max-w-md mx-auto mt-20 p-8 text-center bg-white rounded-xl shadow-md border border-red-100">
    <h1 className="text-4xl font-extrabold text-red-600 mb-2">403</h1>
    <h2 className="text-xl font-bold text-gray-800 mb-2">Truy cập bị từ chối</h2>
    <p className="text-gray-600 mb-6">Bạn không có quyền truy cập vào tài nguyên hoặc trang này.</p>
    <a href="/products" className="inline-block bg-blue-600 text-white px-5 py-2.5 rounded-lg text-sm font-semibold hover:bg-blue-700 transition">
      Về trang sản phẩm
    </a>
  </div>
);

const NotFoundPage = () => (
  <div className="max-w-md mx-auto mt-20 p-8 text-center bg-white rounded-xl shadow-md border border-gray-100">
    <h1 className="text-4xl font-extrabold text-gray-800 mb-2">404</h1>
    <h2 className="text-xl font-bold text-gray-700 mb-2">Không tìm thấy trang</h2>
    <p className="text-gray-500 mb-6">Đường dẫn bạn yêu cầu không tồn tại hoặc đã bị di chuyển.</p>
    <a href="/products" className="inline-block bg-blue-600 text-white px-5 py-2.5 rounded-lg text-sm font-semibold hover:bg-blue-700 transition">
      Về trang chủ
    </a>
  </div>
);

export const AppRouter: React.FC = () => {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-gray-50 text-gray-900">
        <Routes>
          {/* CẤU TRÚC MỚI: Bọc các trang Public bằng PublicLayout */}
          <Route element={<PublicLayout />}>
            {/* Route gốc / sẽ render HomeUser */}
            <Route path="/" element={<HomeUser />} />
            <Route path="/products" element={<ProductListPage />} />
            <Route path="/products/:productId" element={<ProductDetailPage />} />

            {/* Các trang Public khác có thể đưa vào đây nếu muốn có Header/Footer */}
            {/* Ví dụ: CatalogRoutes sẽ cần phải được thiết kế lại chút nếu muốn dùng chung layout này */}
          </Route>

          {/* <Route path="/" element={<Navigate to="/products" replace />} /> */}
          {/* Các route không cần Header/Footer chung (Ví dụ: Trang Login/Register có thiết kế riêng) */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/ai-assistant" element={<AIChatPage />} />
          <Route path="/403" element={<ForbiddenPage />} />

          <Route path="/products/:productId/reviews" element={<ProductReviewsPage />} />

          {/* Catalog Module Routes */}
          <Route path="/*" element={<CatalogRoutes />} />

          {/* User Protected Routes */}
          <Route element={<ProtectedRoute />}>
            <Route path="/profile" element={<ProfilePage />} />
            <Route path="/me/addresses" element={<ProfilePage />} />
            <Route path="/cart" element={<CartPage />} />
            <Route path="/checkout" element={<CheckoutPage />} />
            <Route path="/orders" element={<OrderHistoryPage />} />
            <Route path="/orders/:orderId" element={<OrderDetailPage />} />
            <Route path="/me/order-items/:orderItemId/review" element={<WriteReviewPage />} />
          </Route>

          {/* Staff & Admin Protected Routes */}
          <Route element={<RoleBasedRoute allowedRoles={[ROLES.STAFF, ROLES.ADMIN]} />}>
            {/* ĐÂY LÀ CHỖ THAY ĐỔI: Bọc StaffLayout ra ngoài */}
            <Route element={<StaffLayout />}>
              <Route path="/staff/inventory" element={<InventoryPage />} />
              <Route path="/staff/orders" element={<StaffOrderListPage />} />
              <Route path="/staff/reviews" element={<StaffReviewPage />} />
              <Route path="/staff/*" element={<StaffDashboard />} />
            </Route>
          </Route>

          {/* Admin Only Routes */}
          <Route element={<RoleBasedRoute allowedRoles={[ROLES.ADMIN]} />}>
            <Route path="/admin/promotions/*" element={<PromotionRoutes />} />
            <Route path="/admin/*" element={<AdminDashboard />} />
          </Route>

          {/* 404 Catch-All */}
          <Route path="*" element={<NotFoundPage />} />
        </Routes>

        {/* Floating AI Consultant Assistant Widget on all pages */}
        <AIChatWidget />
      </div>
    </BrowserRouter>
  );
};
