import React, { createContext, useCallback, useContext, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { Icon } from './Icon';

type ToastTone = 'success' | 'error';

interface ToastAction {
  label: string;
  to: string;
}

interface ToastItem {
  id: number;
  message: string;
  tone: ToastTone;
  action?: ToastAction;
}

interface ToastContextValue {
  toast: (message: string, options?: { tone?: ToastTone; action?: ToastAction }) => void;
}

const ToastContext = createContext<ToastContextValue | undefined>(undefined);

/** Thông báo nổi kiểu design (.toast). Phải nằm trong Router nếu dùng action link. */
export const ToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [items, setItems] = useState<ToastItem[]>([]);
  const nextId = useRef(1);

  const toast = useCallback<ToastContextValue['toast']>((message, options) => {
    const id = nextId.current++;
    setItems((prev) => [...prev.slice(-2), { id, message, tone: options?.tone ?? 'success', action: options?.action }]);
    window.setTimeout(() => setItems((prev) => prev.filter((t) => t.id !== id)), 4200);
  }, []);

  return (
    <ToastContext.Provider value={{ toast }}>
      {children}
      <div className="toast-stack" aria-live="polite">
        {items.map((t) => (
          <div key={t.id} className={`toast ${t.tone === 'error' ? 'error' : ''}`} role={t.tone === 'error' ? 'alert' : 'status'}>
            <Icon name={t.tone === 'error' ? 'alert' : 'check'} />
            <span>
              {t.message}
              {t.action && <Link to={t.action.to}>{t.action.label}</Link>}
            </span>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
};

export const useToast = () => {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast must be used within ToastProvider');
  return ctx.toast;
};
