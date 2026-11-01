import React from 'react';
import { Link } from 'react-router-dom';
import { Icon } from '@/components/ui';

interface AuthLayoutProps {
  title: string;
  subtitle: string;
  error?: string | null;
  children: React.ReactNode;
  footer: React.ReactNode;
}

/** Bố cục đăng nhập/đăng ký theo design (.auth-layout): câu chuyện thương hiệu + biểu mẫu. */
export const AuthLayout: React.FC<AuthLayoutProps> = ({ title, subtitle, error, children, footer }) => (
  <div className="ls-container section">
    <div className="auth-layout">
      <div className="auth-story">
        <div className="eyebrow">Công nghệ cho điều bạn muốn</div>
        <h2>Một chiếc laptop.<br />Ngàn điều có thể.</h2>
        <p>Học điều mới. Tạo điều hay.<br />Bắt đầu hành trình cùng LaptopStore.</p>
        <img src="/images/hero-laptop.svg" alt="Laptop cho những ý tưởng mới" />
        <p className="small row gap8"><Icon name="shield" /> Chính hãng · Tận tâm · An toàn</p>
      </div>
      <div className="auth-form">
        <h1>{title}</h1>
        <p>{subtitle}</p>
        {error && (
          <div className="notice red" role="alert" style={{ marginBottom: 18 }}>
            <Icon name="alert" /><div>{error}</div>
          </div>
        )}
        {children}
        <p className="small muted" style={{ textAlign: 'center', margin: '22px 0' }}>{footer}</p>
        <Link to="/" className="text-btn small"><Icon name="arrow-left" /> Quay về cửa hàng</Link>
      </div>
    </div>
  </div>
);
