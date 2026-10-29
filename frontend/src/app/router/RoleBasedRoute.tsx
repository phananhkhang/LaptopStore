import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../providers/AuthProvider";

interface RoleBasedRouteProps {
  allowedRoles: string[];
}

export const RoleBasedRoute: React.FC<RoleBasedRouteProps> = ({
  allowedRoles,
}) => {
  const { user, isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div style={{ padding: "2rem", textAlign: "center" }}>
        Đang kiểm tra quyền truy cập...
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  const normalizeRole = (role: string) => role.replace(/^ROLE_/, "");
  const hasRequiredRole = user?.roles.some((role) =>
    allowedRoles.some(
      (allowed) => normalizeRole(allowed) === normalizeRole(role),
    ),
  );

  if (!hasRequiredRole) {
    return <Navigate to="/403" replace />;
  }

  return <Outlet />;
};
