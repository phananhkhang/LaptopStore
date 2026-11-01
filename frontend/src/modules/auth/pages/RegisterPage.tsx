import { useAuth } from '@/app/providers/AuthProvider';
import { apiErrorMessage, useToast } from '@/components/ui';
import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { AuthLayout } from '../components/AuthLayout';

type FromState = { from?: { pathname?: string; search?: string } };

export const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const toast = useToast();
  const { register } = useAuth();

  const [fullName, setFullName] = useState<string>('');
  const [email, setEmail] = useState<string>('');
  const [phone, setPhone] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [confirmPassword, setConfirmPassword] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const from = (location.state as FromState | null)?.from;
  const redirectPath = from?.pathname && !['/login', '/register'].includes(from.pathname) ? `${from.pathname}${from.search ?? ''}` : '/';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!fullName.trim() || !email.trim() || !password) {
      setError('Vui lòng điền đầy đủ các thông tin bắt buộc (*).');
      return;
    }
    if (password.length < 6) {
      setError('Mật khẩu phải chứa ít nhất 6 ký tự.');
      return;
    }
    if (password !== confirmPassword) {
      setError('Mật khẩu xác nhận không khớp.');
      return;
    }

    try {
      setLoading(true);
      await register({
        fullName: fullName.trim(),
        email: email.trim(),
        phone: phone.trim() ? phone.trim() : undefined,
        password,
      });
      toast('Tạo tài khoản thành công. Chào mừng bạn đến với LaptopStore!');
      navigate(redirectPath, { replace: true });
    } catch (err: unknown) {
      setError(apiErrorMessage(err, 'Đăng ký không thành công. Email có thể đã được sử dụng.'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout
      title="Tạo tài khoản"
      subtitle="Cùng bạn tìm chiếc laptop thật phù hợp."
      error={error}
      footer={<>Đã có tài khoản? <Link to="/login" state={location.state} className="text-btn">Đăng nhập</Link></>}
    >
      <form onSubmit={handleSubmit} noValidate>
        <label className="field">
          <span>Họ và tên *</span>
          <input required maxLength={150} autoComplete="name" placeholder="Nguyễn Văn A" value={fullName} onChange={(e) => setFullName(e.target.value)} disabled={loading} />
        </label>
        <div className="field-row">
          <label className="field">
            <span>Địa chỉ email *</span>
            <input type="email" required autoComplete="email" placeholder="ban@example.com" value={email} onChange={(e) => setEmail(e.target.value)} disabled={loading} />
          </label>
          <label className="field">
            <span>Số điện thoại</span>
            <input type="tel" autoComplete="tel" placeholder="0901234567" value={phone} onChange={(e) => setPhone(e.target.value)} disabled={loading} />
          </label>
        </div>
        <div className="field-row">
          <label className="field">
            <span>Mật khẩu *</span>
            <input type="password" required minLength={6} autoComplete="new-password" placeholder="Tối thiểu 6 ký tự" value={password} onChange={(e) => setPassword(e.target.value)} disabled={loading} />
          </label>
          <label className="field">
            <span>Xác nhận mật khẩu *</span>
            <input type="password" required minLength={6} autoComplete="new-password" placeholder="Nhập lại mật khẩu" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} disabled={loading} />
          </label>
        </div>
        <button type="submit" className="btn primary full" disabled={loading} style={{ minHeight: 48 }}>
          {loading ? 'Đang tạo tài khoản...' : 'Đăng ký'}
        </button>
      </form>
    </AuthLayout>
  );
};
