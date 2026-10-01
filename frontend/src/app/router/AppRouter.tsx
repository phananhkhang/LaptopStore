import React from 'react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from './ProtectedRoute';
import { RoleBasedRoute } from './RoleBasedRoute';
import { ROLES } from '@/constants/roles';

const HomePage = () => <div style={{ padding: '2rem' }}><h1>Trang chủ LaptopStore</h1></div>;
const LoginPage = () => <div style={{ padding: '2rem' }}><h1>Trang Đăng Nhập</h1></div>;
const RegisterPage = () => <div style={{ padding: '2rem' }}><h1>Trang Đăng Ký</h1></div>;
const ProfilePage = () => <div style={{ padding: '2rem' }}><h1>Hồ Sơ Cá Nhân (User Protected)</h1></div>;
const StaffDashboard = () => <div style={{ padding: '2rem' }}><h1>Bàn Làm Việc Nhân Viên (Staff Protected)</h1></div>;
const AdminDashboard = () => <div style={{ padding: '2rem' }}><h1>Trang Quản Trị Hệ Thống (Admin Protected)</h1></div>;
const ForbiddenPage = () => <div style={{ padding: '2rem', color: 'red' }}><h1>403 — Bạn không có quyền truy cập trang này</h1></div>;
const NotFoundPage = () => <div style={{ padding: '2rem' }}><h1>404 — Không tìm thấy trang</h1></div>;

export const AppRouter: React.FC = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/403" element={<ForbiddenPage />} />

        <Route element={<ProtectedRoute />}>
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/me/addresses" element={<ProfilePage />} />
        </Route>

        <Route element={<RoleBasedRoute allowedRoles={[ROLES.STAFF, ROLES.ADMIN]} />}>
          <Route path="/staff/*" element={<StaffDashboard />} />
        </Route>

        <Route element={<RoleBasedRoute allowedRoles={[ROLES.ADMIN]} />}>
          <Route path="/admin/*" element={<AdminDashboard />} />
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </BrowserRouter>
  );
};
