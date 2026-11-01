import { Spinner } from '@/components/ui';
import React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../providers/AuthProvider';

interface RoleBasedRouteProps {
  allowedRoles: string[];
}

export const RoleBasedRoute: React.FC<RoleBasedRouteProps> = ({ allowedRoles }) => {
  const { user, isAuthenticated, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return <div className="ls-container section"><Spinner label="Đang kiểm tra quyền truy cập..." /></div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  const hasRequiredRole = user?.roles.some((role) => allowedRoles.includes(role));

  if (!hasRequiredRole) {
    return <Navigate to="/403" replace />;
  }

  return <Outlet />;
};
