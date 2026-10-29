import React, { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { PrimaryButton, SecondaryButton } from '@/components/common/AdminUI';
import { formatReviewDate, reviewMockStore } from '../../store/reviewMockStore';
import { LaptopIllustration, PurchasedBadge, StarInput } from '../../components/ReviewUI';

const MAX_COMMENT = 2000;

const WriteReviewPage: React.FC = () => {
    const navigate = useNavigate();
    const { orderItemId } = useParams<{ orderItemId: string }>();
    const item = reviewMockStore.getOrderItem(Number(orderItemId));
    const existing = item ? reviewMockStore.getReviewByOrderItem(item.orderItemId) : undefined;

    const [rating, setRating] = useState(existing?.rating ?? 0);
    const [comment, setComment] = useState(existing?.comment ?? '');
    const [error, setError] = useState('');
    const [submitted, setSubmitted] = useState(false);

    if (!item) {
        return (
            <div className="max-w-6xl mx-auto px-4 py-16 text-center">
                <h1 className="text-2xl font-bold text-gray-900">Không tìm thấy sản phẩm cần đánh giá</h1>
                <p className="mt-2 text-gray-500">Bạn chỉ có thể đánh giá sản phẩm thuộc đơn hàng đã giao thành công.</p>
                <SecondaryButton className="mt-6" onClick={() => navigate('/orders')}>
                    Quay lại đơn hàng
                </SecondaryButton>
            </div>
        );
    }

    const handleSubmit = (event: React.FormEvent) => {
        event.preventDefault();
        if (!rating) {
            setError('Vui lòng chọn số sao trước khi gửi đánh giá.');
            return;
        }
        // TODO: thay bằng reviewApi.createReview / updateReview khi nối backend
        reviewMockStore.saveMyReview(item, rating, comment.trim());
        setSubmitted(true);
    };

    return (
        <div className="max-w-6xl mx-auto px-4 py-8 md:py-10">
            <nav className="text-sm text-gray-500">
                <Link to="/products" className="hover:text-[#C2410C]">Cửa hàng</Link>
                <span className="mx-3">/</span>
                <span className="text-gray-900">Đánh giá sản phẩm</span>
            </nav>

            <p className="mt-8 text-xs font-semibold tracking-wide uppercase text-[#C2410C]">Sau mỗi hành trình</p>
            <h1 className="mt-1 text-3xl md:text-4xl font-bold text-gray-900">Chia sẻ trải nghiệm của bạn</h1>
            <p className="mt-2 text-gray-500">Một đánh giá chân thật giúp mọi người chọn laptop dễ dàng hơn.</p>

            <div className="grid grid-cols-1 lg:grid-cols-[minmax(0,5fr)_minmax(0,9fr)] gap-6 mt-8 items-start">
                <aside className="bg-white rounded-2xl border border-gray-200 p-6 md:p-8 text-center">
                    <div className="flex items-center justify-center rounded-2xl bg-gray-50 p-8">
                        <LaptopIllustration />
                    </div>
                    <div className="mt-5">
                        <PurchasedBadge />
                    </div>
                    <h2 className="mt-4 text-2xl font-bold text-gray-900">{item.productName}</h2>
                    <p className="mt-3 text-sm text-gray-500">{item.variantName}</p>
                    <hr className="my-6 border-gray-100" />
                    <p className="font-semibold text-gray-900">Đơn hàng {item.orderCode}</p>
                    <p className="mt-3 text-sm text-gray-500">Đã giao thành công · {formatReviewDate(item.deliveredAt)}</p>
                </aside>

                {submitted ? (
                    <section className="bg-white rounded-2xl border border-gray-200 p-6 md:p-10 text-center">
                        <div className="mx-auto flex items-center justify-center w-16 h-16 rounded-full bg-emerald-50 text-emerald-600">
                            <svg className="w-8 h-8" fill="none" stroke="currentColor" strokeWidth={2.5} viewBox="0 0 24 24">
                                <path d="m5 12.5 4.5 4.5L19 7.5" strokeLinecap="round" strokeLinejoin="round" />
                            </svg>
                        </div>
                        <h2 className="mt-5 text-2xl font-bold text-gray-900">Cảm ơn bạn đã chia sẻ!</h2>
                        <p className="mt-2 text-gray-500">Đánh giá của bạn đã được ghi nhận và hiển thị cho những người mua sau.</p>
                        <div className="flex flex-wrap justify-center gap-4 mt-8">
                            <SecondaryButton onClick={() => navigate('/orders')}>Quay lại đơn hàng</SecondaryButton>
                            <PrimaryButton onClick={() => navigate(`/products/${item.productId}/reviews`)}>Xem đánh giá sản phẩm</PrimaryButton>
                        </div>
                    </section>
                ) : (
                    <form onSubmit={handleSubmit} noValidate className="bg-white rounded-2xl border border-gray-200 p-6 md:p-10">
                        <h2 className="text-2xl md:text-3xl font-bold text-gray-900">Bạn thấy chiếc laptop này thế nào?</h2>

                        <p className="mt-6 mb-5 text-sm font-semibold text-gray-900">Mức độ hài lòng của bạn</p>
                        <StarInput
                            value={rating}
                            onChange={v => {
                                setRating(v);
                                setError('');
                            }}
                        />
                        {error && <p className="mt-1 text-sm text-red-500">{error}</p>}

                        <label className="block mt-6 rounded-2xl border border-gray-200 p-5 focus-within:border-[#C2410C] focus-within:ring-2 focus-within:ring-orange-100 transition">
                            <span className="block text-sm font-semibold text-gray-900">Chia sẻ cảm nhận</span>
                            <textarea
                                value={comment}
                                onChange={e => setComment(e.target.value.slice(0, MAX_COMMENT))}
                                rows={5}
                                placeholder="Hiệu năng, màn hình, pin, dịch vụ giao hàng... bạn thấy thế nào?"
                                className="mt-3 w-full resize-none text-gray-800 placeholder:text-gray-400 focus:outline-none leading-relaxed"
                            />
                            <span className={`block text-xs ${comment.length >= MAX_COMMENT ? 'text-red-500' : 'text-gray-500'}`}>
                                {new Intl.NumberFormat('vi-VN').format(comment.length)} / {new Intl.NumberFormat('vi-VN').format(MAX_COMMENT)} ký tự
                            </span>
                        </label>

                        <div className="flex gap-4 mt-6 p-5 rounded-2xl bg-orange-50">
                            <svg className="w-5 h-5 mt-0.5 shrink-0 text-[#C2410C]" fill="none" stroke="currentColor" strokeWidth={2.5} viewBox="0 0 24 24">
                                <path d="m5 12.5 4.5 4.5L19 7.5" strokeLinecap="round" strokeLinejoin="round" />
                            </svg>
                            <div>
                                <p className="font-semibold text-[#C2410C]">Đánh giá gắn với sản phẩm bạn đã mua</p>
                                <p className="mt-1 text-sm text-gray-600">Bạn có thể chỉnh sửa nội dung đánh giá của mình sau khi gửi.</p>
                            </div>
                        </div>

                        <div className="flex flex-wrap justify-between gap-4 mt-6">
                            <SecondaryButton type="button" className="min-w-[200px]" onClick={() => navigate('/orders')}>
                                Quay lại đơn hàng
                            </SecondaryButton>
                            <PrimaryButton type="submit" className="min-w-[200px]">
                                {existing ? 'Cập nhật đánh giá' : 'Gửi đánh giá'}
                            </PrimaryButton>
                        </div>
                    </form>
                )}
            </div>
        </div>
    );
};

export default WriteReviewPage;
