import React from 'react';

export const PageHeader: React.FC<{
    breadcrumb: string;
    title: string;
    subtitle: string;
    action?: React.ReactNode;
}> = ({ breadcrumb, title, subtitle, action }) => (
    <div className="flex flex-wrap items-start justify-between gap-4 mb-8">
        <div>
            <p className="text-xs font-semibold tracking-wide uppercase text-[#C2410C]">{breadcrumb}</p>
            <h1 className="mt-1 text-3xl font-bold text-gray-900">{title}</h1>
            <p className="mt-2 text-sm text-gray-500">{subtitle}</p>
        </div>
        {action}
    </div>
);

export const PrimaryButton: React.FC<React.ButtonHTMLAttributes<HTMLButtonElement>> = ({ className = '', ...props }) => (
    <button
        {...props}
        className={`px-6 py-3 rounded-xl bg-[#C2410C] text-white text-sm font-semibold shadow-sm hover:bg-[#9A3412] disabled:opacity-50 disabled:cursor-not-allowed transition ${className}`}
    />
);

export const SecondaryButton: React.FC<React.ButtonHTMLAttributes<HTMLButtonElement>> = ({ className = '', ...props }) => (
    <button
        {...props}
        className={`px-6 py-3 rounded-xl border border-gray-200 bg-white text-gray-900 text-sm font-semibold hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition ${className}`}
    />
);

export const StatCard: React.FC<{ label: string; value: string; note: string }> = ({ label, value, note }) => (
    <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
        <p className="text-sm text-gray-500">{label}</p>
        <p className="mt-3 text-3xl font-bold text-gray-900">{value}</p>
        <p className="mt-3 text-xs font-semibold text-emerald-700">{note}</p>
    </div>
);

export const badgeTone = {
    green: 'bg-emerald-50 text-emerald-700',
    gray: 'bg-gray-100 text-gray-500',
    orange: 'bg-orange-50 text-[#C2410C]',
    blue: 'bg-blue-50 text-blue-700'
};

export const Badge: React.FC<{ tone: keyof typeof badgeTone; children: React.ReactNode }> = ({ tone, children }) => (
    <span className={`inline-block px-3 py-1.5 rounded-lg text-xs font-semibold ${badgeTone[tone]}`}>{children}</span>
);

export const SearchInput: React.FC<{ value: string; onChange: (value: string) => void; placeholder: string }> = ({ value, onChange, placeholder }) => (
    <div className="relative w-full max-w-md">
        <svg className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
            <circle cx="11" cy="11" r="7" />
            <path d="m20 20-3.5-3.5" strokeLinecap="round" />
        </svg>
        <input
            value={value}
            onChange={e => onChange(e.target.value)}
            placeholder={placeholder}
            className="w-full pl-11 pr-4 py-3 rounded-xl border border-gray-200 bg-white text-sm placeholder:text-gray-400 focus:outline-none focus:border-[#C2410C] focus:ring-2 focus:ring-orange-100"
        />
    </div>
);

export const SelectFilter: React.FC<{
    value: string;
    onChange: (value: string) => void;
    options: { value: string; label: string }[];
}> = ({ value, onChange, options }) => (
    <select
        value={value}
        onChange={e => onChange(e.target.value)}
        className="px-4 py-3 pr-10 rounded-xl border border-gray-200 bg-white text-sm font-medium text-gray-900 focus:outline-none focus:border-[#C2410C] focus:ring-2 focus:ring-orange-100 cursor-pointer"
    >
        {options.map(o => (
            <option key={o.value} value={o.value}>{o.label}</option>
        ))}
    </select>
);

const VISIBLE_PAGES = 3;

// Cửa sổ 3 trang trượt theo trang hiện tại: [1 2 3] → [2 3 4] → ... và dừng ở hai đầu
const getVisiblePages = (page: number, totalPages: number): number[] => {
    const count = Math.min(VISIBLE_PAGES, totalPages);
    const start = Math.min(Math.max(1, page - Math.floor(count / 2)), totalPages - count + 1);
    return Array.from({ length: count }, (_, i) => start + i);
};

const ChevronIcon: React.FC<{ direction: 'left' | 'right' }> = ({ direction }) => (
    <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2.2} viewBox="0 0 24 24">
        <path d={direction === 'left' ? 'm15 18-6-6 6-6' : 'm9 18 6-6-6-6'} strokeLinecap="round" strokeLinejoin="round" />
    </svg>
);

export const Pagination: React.FC<{
    total: number;
    page: number;
    pageSize: number;
    onPageChange: (page: number) => void;
}> = ({ total, page, pageSize, onPageChange }) => {
    const totalPages = Math.max(1, Math.ceil(total / pageSize));
    const from = total === 0 ? 0 : (page - 1) * pageSize + 1;
    const to = Math.min(page * pageSize, total);
    const navBtn =
        'flex items-center justify-center w-9 h-9 rounded-full text-gray-600 hover:bg-orange-50 hover:text-[#C2410C] disabled:text-gray-300 disabled:hover:bg-transparent disabled:cursor-not-allowed transition';

    return (
        <div className="flex flex-wrap items-center gap-4 mt-6">
            <p className="text-sm text-gray-500">
                Hiển thị <span className="font-semibold text-gray-800">{from}–{to}</span> trên{' '}
                <span className="font-semibold text-gray-800">{total}</span> kết quả
            </p>
            <nav className="flex items-center gap-1 p-1 rounded-full bg-white border border-gray-100 shadow-sm" aria-label="Phân trang">
                <button className={navBtn} disabled={page <= 1} onClick={() => onPageChange(page - 1)} aria-label="Trang trước">
                    <ChevronIcon direction="left" />
                </button>
                {getVisiblePages(page, totalPages).map(p => (
                    <button
                        key={p}
                        onClick={() => onPageChange(p)}
                        aria-current={p === page ? 'page' : undefined}
                        className={`min-w-[2.25rem] h-9 px-3 rounded-full text-sm font-semibold transition ${
                            p === page ? 'bg-[#C2410C] text-white shadow-sm' : 'text-gray-600 hover:bg-orange-50 hover:text-[#C2410C]'
                        }`}
                    >
                        {p}
                    </button>
                ))}
                <button className={navBtn} disabled={page >= totalPages} onClick={() => onPageChange(page + 1)} aria-label="Trang sau">
                    <ChevronIcon direction="right" />
                </button>
            </nav>
        </div>
    );
};

// Bảng chung: header xám nhạt, hàng cao, viền mảnh như thiết kế
export const DataTable: React.FC<{ headers: string[]; children: React.ReactNode; emptyText: string; isEmpty: boolean }> = ({
    headers,
    children,
    emptyText,
    isEmpty
}) => (
    <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-x-auto">
        <table className="min-w-full text-sm">
            <thead className="bg-gray-50">
                <tr>
                    {headers.map(h => (
                        <th key={h} className="px-4 py-4 text-left font-semibold text-gray-500 whitespace-nowrap">{h}</th>
                    ))}
                </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
                {isEmpty ? (
                    <tr>
                        <td colSpan={headers.length} className="px-4 py-12 text-center text-gray-400">{emptyText}</td>
                    </tr>
                ) : (
                    children
                )}
            </tbody>
        </table>
    </div>
);
