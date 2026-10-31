import { resolveImageUrl } from '@/utils/image';
import React from 'react';
import { Link } from 'react-router-dom';

export const HomeUser: React.FC = () => {
    return (
        <div className="flex flex-col items-center py-7 pb-12 gap-8 w-full max-w-[1280px] mx-auto font-sans">

            {/* 1. Hero Section */}
            <section className="w-full flex flex-col md:flex-row items-center p-10 bg-primary-light rounded-[24px] min-h-[460px] relative overflow-hidden">
                <div className="flex flex-col items-start gap-5 w-full md:w-[516px] z-10">
                    <span className="text-primary text-[11px] font-semibold uppercase tracking-wider">Mở máy lên. Mở điều mới.</span>
                    <h1 className="text-ink text-[40px] md:text-[52px] font-semibold leading-[1.25]">Chiếc laptop cho<br />mọi <span className="text-primary text-[40px] md:text-[52px] font-semibold leading-[1.25]">ý tưởng.</span></h1>
                    <p className="text-ink-lighter text-[14px] leading-relaxed max-w-[480px]">Học tập, sáng tạo hay chinh phục trận đấu.<br />Tìm người bạn đồng hành hoàn hảo của bạn.</p>
                    <div className="flex flex-wrap gap-3 mt-1">
                        <Link to="/products" className="bg-primary text-white px-[24px] py-[12px] rounded-xl font-semibold text-[13px] flex justify-center items-center min-w-[170px] hover:bg-primary-dark transition-colors">Khám phá ngay</Link>
                        <Link to="/ai-assistant" className="bg-white text-ink border border-surface-border px-[24px] py-[12px] rounded-xl font-semibold text-[13px] flex justify-center items-center min-w-[170px] hover:bg-gray-50 transition-colors">Tư vấn cùng AI</Link>
                    </div>
                </div>

                {/* Hero Artwork Placeholder */}
                <div className="flex-1 flex justify-center items-center mt-10 md:mt-0 z-0">
                <img
                    src={resolveImageUrl('/uploads/products/product-01.jpg')}
                    alt="Hero Laptop"
                    className="w-full max-w-[500px] object-contain drop-shadow-2xl mix-blend-multiply"
                />
                </div>
            </section>

            {/* 2. Service Promises */}
            <section className="w-full flex flex-wrap lg:flex-nowrap items-center justify-between p-4 bg-white rounded-2xl shadow-sm border border-surface-border gap-4">
                <div className="flex items-center gap-3 w-full lg:w-auto">
                    <div className="w-[26px] h-[26px] rounded-full border-[1.8px] border-primary text-primary flex items-center justify-center">
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                    </div>
                    <div className="flex flex-col">
                        <span className="text-ink text-[12px] font-semibold">Chính hãng 100%</span>
                        <span className="text-ink-lighter text-[11px]">Yên tâm chọn lựa</span>
                    </div>
                </div>

                <div className="flex items-center gap-3 w-full lg:w-auto">
                    <div className="w-[26px] h-[26px] rounded-full border-[1.8px] border-primary text-primary flex items-center justify-center">
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" /></svg>
                    </div>
                    <div className="flex flex-col">
                        <span className="text-ink text-[12px] font-semibold">Giao hàng toàn quốc</span>
                        <span className="text-ink-lighter text-[11px]">Nhanh chóng, tận nơi</span>
                    </div>
                </div>

                <div className="flex items-center gap-3 w-full lg:w-auto">
                    <div className="w-[26px] h-[26px] rounded-full border-[1.8px] border-primary text-primary flex items-center justify-center">
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" /></svg>
                    </div>
                    <div className="flex flex-col">
                        <span className="text-ink text-[12px] font-semibold">Thanh toán linh hoạt</span>
                        <span className="text-ink-lighter text-[11px]">COD · VNPay</span>
                    </div>
                </div>

                <div className="flex items-center gap-3 w-full lg:w-auto">
                    <div className="w-[26px] h-[26px] rounded-full border-[1.8px] border-primary text-primary flex items-center justify-center">
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18.364 5.636l-3.536 3.536m0 5.656l3.536 3.536M9.172 9.172L5.636 5.636m3.536 9.192l-3.536 3.536M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-5 0a4 4 0 11-8 0 4 4 0 018 0z" /></svg>
                    </div>
                    <div className="flex flex-col">
                        <span className="text-ink text-[12px] font-semibold">Luôn sẵn sàng hỗ trợ</span>
                        <span className="text-ink-lighter text-[11px]">Tư vấn dành riêng cho bạn</span>
                    </div>
                </div>
            </section>

            {/* 3. Section Heading & Tabs */}
            <div className="w-full flex flex-col gap-4">
                <div className="w-full flex justify-between items-end">
                    <div className="flex flex-col gap-1">
                        <h2 className="text-ink text-[22px] font-semibold">Đúng nhu cầu. Đúng chiếc máy.</h2>
                        <p className="text-ink-lighter text-[13px]">Những lựa chọn nổi bật dành cho bạn</p>
                    </div>
                    <Link to="/products" className="hidden md:flex bg-white border border-surface-border text-ink px-[24px] py-[12px] rounded-xl text-[13px] font-semibold hover:bg-gray-50 transition-colors">Xem tất cả laptop</Link>
                </div>

                <div className="w-full flex gap-3 overflow-x-auto pb-2 scrollbar-hide">
                    <button className="bg-primary text-white px-[24px] py-[12px] rounded-xl text-[13px] font-semibold whitespace-nowrap min-w-[128px]">Tất cả laptop</button>
                    <button className="bg-white border border-surface-border text-ink px-[24px] py-[12px] rounded-xl text-[13px] font-semibold whitespace-nowrap min-w-[126px] hover:bg-gray-50">Gaming</button>
                    <button className="bg-white border border-surface-border text-ink px-[24px] py-[12px] rounded-xl text-[13px] font-semibold whitespace-nowrap min-w-[126px] hover:bg-gray-50">Văn phòng</button>
                    <button className="bg-white border border-surface-border text-ink px-[24px] py-[12px] rounded-xl text-[13px] font-semibold whitespace-nowrap min-w-[126px] hover:bg-gray-50">Mỏng nhẹ</button>
                </div>
            </div>

            {/* 4. Product Grid (Mock UI) */}
            <section className="w-full grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
                {[
                    { id: 1, tag: 'GAMING', name: 'ASUS ROG Strix G15', spec: 'i7 · RTX 3060 · 16GB · 512GB', price: '25.000.000₫', img: '02' },
                    { id: 2, tag: 'MỎNG NHẸ', name: 'Dell XPS 13', spec: 'Core i7 · 16GB · 512GB', price: '35.000.000₫', img: '03' },
                    { id: 3, tag: 'GAMING', name: 'ROG Strix G15 / 32GB', spec: 'i7 · RTX 3060 · 32GB · 1TB', price: '30.000.000₫', img: '04' },
                    { id: 4, tag: 'VĂN PHÒNG', name: 'Laptop cho ngày sáng tạo', spec: 'Màn hình đẹp · Pin cả ngày', price: 'Trải nghiệm ngay', img: '05' },
                ].map((item) => (
                    <div key={item.id} className="bg-white border border-surface-border rounded-[16px] p-4 flex flex-col gap-3 hover:shadow-md transition-shadow group">
                        <div className="w-full h-[172px] bg-surface-gray rounded-xl flex justify-center items-center overflow-hidden">
                            <img src={resolveImageUrl(`/uploads/products/product-${item.img}.jpg`)} alt={item.name} className="max-h-[140px] object-contain mix-blend-multiply group-hover:scale-105 transition-transform duration-300" />
                        </div>

                        <div className="flex flex-col gap-1 mt-1 flex-1">
                            <span className="text-primary text-[10px] font-semibold uppercase">{item.tag}</span>
                            <h3 className="text-ink text-[16px] font-semibold line-clamp-1">{item.name}</h3>
                            <p className="text-ink-lighter text-[11px] truncate">{item.spec}</p>
                            <span className="text-ink text-[18px] font-semibold mt-1">{item.price}</span>
                        </div>

                        <div className="flex items-center justify-between mt-2 pt-1 border-t border-surface-border/50">
                            <span className="text-ink-lighter text-[11px]">★ 4.9 · Chính hãng</span>
                            <Link to={`/products/${item.id}`} className="bg-white border border-surface-border text-ink px-4 py-1.5 rounded-xl text-[13px] font-semibold hover:bg-primary hover:text-white hover:border-primary transition-colors">Xem</Link>
                        </div>
                    </div>
                ))}
            </section>

            {/* 5. Collections */}
            <section className="w-full grid grid-cols-1 lg:grid-cols-2 gap-6 mt-2">
                <div className="bg-primary-light rounded-[20px] p-[28px] flex flex-col-reverse sm:flex-row items-center justify-between min-h-[280px]">
                    <div className="flex flex-col gap-3 w-full sm:w-[260px] mt-6 sm:mt-0">
                        <span className="text-primary text-[10px] font-semibold uppercase tracking-wider">Work Anywhere</span>
                        <h3 className="text-ink text-[24px] font-semibold leading-tight">Nhẹ hành trang.<br/>Rộng cảm hứng.</h3>
                        <p className="text-ink-lighter text-[12px]">Không gian làm việc theo bạn đến mọi nơi.</p>
                        <Link to="/products?category=office" className="text-primary text-[13px] font-semibold mt-2 hover:underline">Laptop mỏng nhẹ →</Link>
                    </div>

                    <div className="w-full sm:w-[310px] flex justify-center">
                        <img src={resolveImageUrl('/uploads/products/product-06.jpg')} className="max-h-[200px] object-contain mix-blend-multiply" alt="Work laptop" />
                    </div>
                </div>

                <div className="bg-surface-gray rounded-[20px] p-[28px] flex flex-col-reverse sm:flex-row items-center justify-between min-h-[280px]">
                    <div className="flex flex-col gap-3 w-full sm:w-[260px] mt-6 sm:mt-0">
                        <span className="text-primary text-[10px] font-semibold uppercase tracking-wider">Play Your Way</span>
                        <h3 className="text-ink text-[24px] font-semibold leading-tight">Sẵn sàng cho<br/>trận đấu lớn.</h3>
                        <p className="text-ink-lighter text-[12px]">Cấu hình mạnh. Từng khung hình mượt mà.</p>
                        <Link to="/products?category=gaming" className="text-primary text-[13px] font-semibold mt-2 hover:underline">Khám phá Gaming →</Link>
                    </div>

                    <div className="w-full sm:w-[310px] flex justify-center">
                        <img src={resolveImageUrl('/uploads/products/product-07.jpg')} className="max-h-[200px] object-contain mix-blend-multiply" alt="Gaming laptop" />
                    </div>
                </div>
            </section>

            {/* 6. AI Banner */}
            <section className="w-full bg-white border border-surface-border rounded-[16px] p-6 flex flex-col sm:flex-row items-center justify-between gap-6 mt-2">
                <div className="flex items-center gap-6">
                    <div className="w-[40px] h-[40px] rounded-full border-[1.8px] border-primary flex items-center justify-center text-primary text-xl shadow-sm">✨</div>
                    <div className="flex flex-col gap-1">
                        <h3 className="text-ink text-[20px] font-semibold">Chưa biết chọn máy nào?</h3>
                        <p className="text-ink-lighter text-[13px]">Kể cho AI nghe nhu cầu và ngân sách của bạn.</p>
                    </div>
                </div>

                <Link to="/ai-assistant" className="w-full sm:w-auto bg-primary text-white px-[24px] py-[12px] rounded-xl text-[13px] font-semibold text-center hover:bg-primary-dark transition-colors">Tư vấn AI</Link>
            </section>

        </div>
    );
};