import React from 'react';
import { BrowserRouter, Route, Routes, Navigate } from 'react-router-dom';
import { ProtectedRoute } from './ProtectedRoute';
import { RoleBasedRoute } from './RoleBasedRoute';
import { ROLES } from '@/constants/roles';

// Auth & User Pages
import { LoginPage } from '@/modules/auth/pages/LoginPage';
import { RegisterPage } from '@/modules/auth/pages/RegisterPage';
import { ProfilePage } from '@/modules/user/pages/ProfilePage';

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
          {/* Public Routes */}
          <Route path="/" element={<Navigate to="/products" replace />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/ai-assistant" element={<AIChatPage />} />
          <Route path="/403" element={<ForbiddenPage />} />

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
          </Route>

          {/* Staff & Admin Protected Routes */}
          <Route element={<RoleBasedRoute allowedRoles={[ROLES.STAFF, ROLES.ADMIN]} />}>
            <Route path="/staff/orders" element={<StaffOrderListPage />} />
            <Route path="/staff/*" element={<StaffDashboard />} />
          </Route>

          {/* Admin Only Routes */}
          <Route element={<RoleBasedRoute allowedRoles={[ROLES.ADMIN]} />}>
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
