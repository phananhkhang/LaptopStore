import React, { useState } from 'react';
import { Badge } from '@/components/common/AdminUI';

const STAR_PATH = 'M12 2.5l2.94 6.1 6.56.9-4.78 4.6 1.17 6.6L12 17.6l-5.89 3.1 1.17-6.6L2.5 9.5l6.56-.9L12 2.5z';

export const ratingLabels: Record<number, string> = {
    1: 'Rất tệ',
    2: 'Chưa hài lòng',
    3: 'Bình thường',
    4: 'Hài lòng',
    5: 'Rất hài lòng'
};

const Star: React.FC<{ filled: boolean; className: string }> = ({ filled, className }) => (
    <svg viewBox="0 0 24 24" className={className} aria-hidden="true">
        <path d={STAR_PATH} fill={filled ? 'currentColor' : 'none'} stroke="currentColor" strokeWidth={filled ? 0 : 1.5} strokeLinejoin="round" />
    </svg>
);

const sizeClass = { sm: 'w-4 h-4', md: 'w-6 h-6', lg: 'w-12 h-12' };

export const StarRating: React.FC<{ rating: number; size?: keyof typeof sizeClass; className?: string }> = ({
    rating,
    size = 'md',
    className = 'text-[#C2410C]'
}) => (
    <div className={`flex items-center gap-0.5 ${className}`} aria-label={`${rating} trên 5 sao`}>
        {[1, 2, 3, 4, 5].map(i => (
            <Star key={i} filled={i <= Math.round(rating)} className={sizeClass[size]} />
        ))}
    </div>
);

// Chọn số sao: rê chuột để xem trước, bấm để chọn, hỗ trợ phím mũi tên
export const StarInput: React.FC<{ value: number; onChange: (value: number) => void }> = ({ value, onChange }) => {
    const [hover, setHover] = useState(0);
    const shown = hover || value;

    return (
        <div>
            <div
                role="radiogroup"
                aria-label="Mức độ hài lòng"
                className="flex items-center gap-4"
                onMouseLeave={() => setHover(0)}
                onKeyDown={e => {
                    if (e.key === 'ArrowRight' || e.key === 'ArrowUp') onChange(Math.min(5, (value || 0) + 1));
                    if (e.key === 'ArrowLeft' || e.key === 'ArrowDown') onChange(Math.max(1, (value || 2) - 1));
                }}
            >
                {[1, 2, 3, 4, 5].map(i => (
                    <button
                        key={i}
                        type="button"
                        role="radio"
                        aria-checked={value === i}
                        aria-label={`${i} sao – ${ratingLabels[i]}`}
                        tabIndex={value === i || (!value && i === 1) ? 0 : -1}
                        onMouseEnter={() => setHover(i)}
                        onClick={() => onChange(i)}
                        className={`rounded-lg transition-transform hover:scale-110 focus:outline-none focus-visible:ring-2 focus-visible:ring-orange-200 ${
                            i <= shown ? 'text-[#C2410C]' : 'text-gray-300'
                        }`}
                    >
                        <Star filled={i <= shown} className={sizeClass.lg} />
                    </button>
                ))}
            </div>
            <p className={`mt-5 h-6 font-semibold ${shown ? 'text-[#C2410C]' : 'text-gray-400'}`}>
                {shown ? ratingLabels[shown] : 'Chọn số sao để đánh giá'}
            </p>
        </div>
    );
};

export const Avatar: React.FC<{ name: string }> = ({ name }) => {
    const words = name.trim().split(/\s+/);
    const initials = (words.length > 1 ? words[words.length - 2][0] + words[words.length - 1][0] : words[0].slice(0, 2)).toUpperCase();
    return (
        <div className="flex items-center justify-center w-12 h-12 rounded-xl bg-orange-50 text-[#C2410C] font-bold shrink-0">{initials}</div>
    );
};

export const PurchasedBadge: React.FC = () => <Badge tone="green">Đã mua hàng</Badge>;

export const VisibilityBadge: React.FC<{ visible: boolean }> = ({ visible }) =>
    visible ? <Badge tone="green">Đang hiển thị</Badge> : <Badge tone="gray">Đã ẩn</Badge>;

// Minh hoạ laptop bằng SVG, dùng khi sản phẩm chưa có ảnh
export const LaptopIllustration: React.FC = () => (
    <svg viewBox="0 0 320 220" className="w-full max-w-xs" aria-hidden="true">
        <defs>
            <linearGradient id="screen" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0" stopColor="#FED7AA" />
                <stop offset="0.55" stopColor="#FB923C" />
                <stop offset="1" stopColor="#C2410C" />
            </linearGradient>
        </defs>
        <ellipse cx="160" cy="200" rx="130" ry="8" fill="#E5E7EB" />
        <rect x="58" y="22" width="204" height="138" rx="10" fill="#374151" />
        <rect x="68" y="32" width="184" height="118" rx="4" fill="url(#screen)" />
        <path d="M68 120c40-30 70 10 110-20s54-40 74-30v80H68z" fill="#FFEDD5" opacity="0.55" />
        <path d="M30 168h260l-14 24H44z" fill="#D1D5DB" />
        <rect x="132" y="172" width="56" height="10" rx="3" fill="#E5E7EB" />
    </svg>
);
