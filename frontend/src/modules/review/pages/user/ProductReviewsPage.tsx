import React, { useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Pagination, SecondaryButton } from '@/components/common/AdminUI';
import { formatReviewDate, reviewMockStore } from '../../store/reviewMockStore';
import { PurchasedBadge, StarRating } from '../../components/ReviewUI';

const PAGE_SIZE = 5;

const ProductReviewsPage: React.FC = () => {
    const navigate = useNavigate();
    const { productId } = useParams<{ productId: string }>();
    const id = Number(productId);
    const product = reviewMockStore.getProduct(id);

    const [starFilter, setStarFilter] = useState<number | null>(null);
    const [page, setPage] = useState(1);

    const summary = useMemo(() => reviewMockStore.getSummary(id), [id]);
    const reviews = useMemo(() => reviewMockStore.getVisibleByProduct(id), [id]);
    const filtered = starFilter ? reviews.filter(r => r.rating === starFilter) : reviews;
    const paged = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

    const selectStar = (star: number | null) => {
        setStarFilter(star);
        setPage(1);
    };

    return (
        <div className="max-w-6xl mx-auto px-4 py-8 md:py-10">
            <h1 className="text-3xl font-bold text-gray-900">Đánh giá từ người đã mua</h1>
            <p className="mt-2 text-gray-500">Những trải nghiệm giúp bạn chọn máy tự tin hơn.</p>

            <h2 className="mt-10 text-center text-3xl font-bold text-gray-900">{product?.productName ?? 'Sản phẩm'}</h2>

            <section className="mt-8 bg-white rounded-2xl border border-gray-200 p-6 md:p-10 grid grid-cols-1 md:grid-cols-2 gap-8 items-center">
                <div>
                    <p className="text-5xl md:text-6xl font-bold text-[#C2410C]">
                        {summary.totalReviews ? summary.averageRating.toFixed(1) : '–'} / 5
                    </p>
                    <div className="flex flex-wrap items-center gap-4 mt-6">
                        <StarRating rating={summary.averageRating} size="sm" className="text-gray-900" />
                        <p className="font-semibold text-gray-900">{summary.totalReviews} đánh giá · Từ khách đã nhận hàng</p>
                    </div>
                </div>

                {/* Phân bố số sao, bấm vào một dòng để lọc */}
                <div className="space-y-2">
                    {([5, 4, 3, 2, 1] as const).map(star => {
                        const count = summary.ratingCounts[star];
                        const percent = summary.totalReviews ? (count / summary.totalReviews) * 100 : 0;
                        const active = starFilter === star;
                        return (
                            <button
                                key={star}
                                onClick={() => selectStar(active ? null : star)}
                                disabled={!count}
                                className={`w-full flex items-center gap-3 px-3 py-1.5 rounded-lg text-sm transition disabled:opacity-40 disabled:cursor-default ${
                                    active ? 'bg-orange-50' : 'hover:bg-gray-50'
                                }`}
                            >
                                <span className="w-10 text-left font-medium text-gray-700">{star} ★</span>
                                <span className="flex-1 h-2 rounded-full bg-gray-100 overflow-hidden">
                                    <span className="block h-full rounded-full bg-[#C2410C]" style={{ width: `${percent}%` }} />
                                </span>
                                <span className="w-8 text-right text-gray-500">{count}</span>
                            </button>
                        );
                    })}
                </div>
            </section>

            {starFilter && (
                <div className="flex items-center gap-3 mt-6 text-sm text-gray-600">
                    Đang xem đánh giá {starFilter} sao
                    <button onClick={() => selectStar(null)} className="font-semibold text-[#C2410C] hover:underline">
                        Bỏ lọc
                    </button>
                </div>
            )}

            <div className="mt-8 space-y-8">
                {paged.length === 0 ? (
                    <div className="bg-white rounded-2xl border border-gray-200 p-12 text-center text-gray-400">
                        Sản phẩm chưa có đánh giá nào.
                    </div>
                ) : (
                    paged.map(r => (
                        <article key={r.id} className="bg-white rounded-2xl border border-gray-200 p-6 md:p-8">
                            <div className="flex flex-wrap items-start justify-between gap-3">
                                <div className="flex flex-wrap items-center gap-6">
                                    <p className="font-semibold text-gray-900">{r.customerName}</p>
                                    <PurchasedBadge />
                                </div>
                                <time className="text-sm text-gray-500">{formatReviewDate(r.createdAt)}</time>
                            </div>
                            <StarRating rating={r.rating} className="mt-6 text-[#C2410C]" />
                            {r.comment && <p className="mt-5 text-gray-800 leading-relaxed">{r.comment}</p>}
                        </article>
                    ))
                )}
            </div>

            {filtered.length > PAGE_SIZE && (
                <Pagination total={filtered.length} page={page} pageSize={PAGE_SIZE} onPageChange={setPage} />
            )}

            <div className="flex justify-center mt-10">
                <SecondaryButton className="min-w-[280px]" onClick={() => navigate(`/products/${id}`)}>
                    Quay lại sản phẩm
                </SecondaryButton>
            </div>
        </div>
    );
};

export default ProductReviewsPage;
