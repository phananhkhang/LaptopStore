import { useAuth } from '@/app/providers/AuthProvider';
import { apiErrorMessage, useToast } from '@/components/ui';
import React, { useState } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { AuthLayout } from '../components/AuthLayout';

type FromState = { from?: { pathname?: string; search?: string } };

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const toast = useToast();
  const { login, isAuthenticated, isLoading } = useAuth();

  const [email, setEmail] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const from = (location.state as FromState | null)?.from;
  const redirectPath = from?.pathname && from.pathname !== '/login' ? `${from.pathname}${from.search ?? ''}` : '/';

  if (!isLoading && isAuthenticated && !loading) {
    return <Navigate to={redirectPath} replace />;
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!email.trim() || !password) {
      setError('Vui lòng nhập đầy đủ email và mật khẩu.');
      return;
    }

    try {
      setLoading(true);
      await login({ email: email.trim(), password });
      toast('Đăng nhập thành công. Chào mừng bạn trở lại!');
      navigate(redirectPath, { replace: true });
    } catch (err: unknown) {
      setError(apiErrorMessage(err, 'Đăng nhập không thành công. Vui lòng kiểm tra lại email và mật khẩu.'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout
      title="Chào mừng trở lại"
      subtitle="Đăng nhập để tiếp tục hành trình của bạn."
      error={error}
      footer={<>Chưa có tài khoản? <Link to="/register" state={location.state} className="text-btn">Đăng ký</Link></>}
    >
      <form onSubmit={handleSubmit} noValidate>
        <label className="field">
          <span>Địa chỉ email</span>
          <input type="email" required autoComplete="email" placeholder="ban@example.com" value={email} onChange={(e) => setEmail(e.target.value)} disabled={loading} />
        </label>
        <label className="field">
          <span className="row between">Mật khẩu
            <button type="button" className="text-btn small" onClick={() => setShowPassword((v) => !v)}>{showPassword ? 'Ẩn' : 'Hiện'}</button>
          </span>
          <input type={showPassword ? 'text' : 'password'} required autoComplete="current-password" placeholder="••••••••" value={password} onChange={(e) => setPassword(e.target.value)} disabled={loading} />
        </label>
        <button type="submit" className="btn primary full" disabled={loading} style={{ minHeight: 48 }}>
          {loading ? 'Đang xác thực...' : 'Đăng nhập'}
        </button>
      </form>
    </AuthLayout>
  );
};
