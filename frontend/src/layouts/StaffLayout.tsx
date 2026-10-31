import React from 'react';
import { Outlet, Link, useLocation } from 'react-router-dom';

export const StaffLayout: React.FC = () => {
  const location = useLocation();

  // Hàm helper để check active menu
  const isActive = (path: string) => location.pathname.includes(path);

  const menuItems = [
    { path: '/staff/dashboard', label: 'Tổng quan', icon: 'M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z' },
    { path: '/staff/orders', label: 'Đơn hàng', icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2' },
    { path: '/staff/inventory', label: 'Kho hàng', icon: 'M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4' },
    { path: '/staff/procurement', label: 'Nhập hàng', icon: 'M8 14v3m4-3v3m4-3v3M3 21h18M3 10h18M3 7l9-4 9 4M4 10h16v11H4V10z' },
  ];

  return (
    <div className="flex h-screen bg-surface-gray font-sans overflow-hidden w-full">
      {/* Sidebar */}
      <aside className="w-[240px] bg-white border-r border-surface-border flex flex-col p-4 gap-1 flex-shrink-0 z-10">
        <Link to="/staff/dashboard" className="flex items-center gap-2 p-2 mb-2">
          <div className="w-[26px] h-[26px] border-[1.8px] border-primary rounded flex items-center justify-center">
            <span className="text-[10px] font-bold text-primary">LS</span>
          </div>
          <span className="text-ink text-[18px] font-semibold">laptopstore</span>
        </Link>
        
        <div className="px-3 py-2 text-ink-lighter text-[10px] font-semibold uppercase tracking-wider">
          Vận hành cửa hàng
        </div>
        
        <nav className="flex flex-col gap-1 flex-1">
          {menuItems.map((item) => (
            <Link 
              key={item.path}
              to={item.path}
              className={`flex items-center gap-3 px-3 py-3 rounded-lg transition-colors ${
                isActive(item.path) 
                  ? 'bg-primary-light text-primary' 
                  : 'text-ink-lighter hover:bg-surface-gray hover:text-ink'
              }`}
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.8} d={item.icon} />
              </svg>
              <span className="text-[12px] font-semibold">{item.label}</span>
            </Link>
          ))}
        </nav>
      </aside>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col h-screen overflow-hidden">
        {/* Header */}
        <header className="h-[80px] bg-white border-b border-surface-border px-6 flex items-center justify-between flex-shrink-0 z-10">
          <span className="text-ink-lighter text-[14px] font-medium">Không gian nhân viên</span>
          
          <div className="flex items-center gap-4">
             {/* Avatar */}
             <div className="flex items-center gap-3 bg-white px-2 py-1.5 rounded-xl border border-surface-border cursor-pointer hover:bg-surface-gray transition-colors">
                <div className="w-9 h-9 bg-primary-light rounded-xl flex items-center justify-center text-primary font-bold text-[12px]">AK</div>
                <div className="flex flex-col pr-2">
                   <span className="text-ink text-[13px] font-semibold leading-tight">Anh Khang</span>
                   <span className="text-ink-lighter text-[10px] font-medium">Kho & Vận hành</span>
                </div>
             </div>
          </div>
        </header>

        {/* Dynamic Content (Pages) */}
        <main className="flex-1 overflow-y-auto p-8 bg-surface-gray">
          <Outlet />
        </main>
      </div>
    </div>
  );
};