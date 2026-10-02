import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { CatalogRoutes } from './modules/catalog/routes/CatalogRoutes';

function App() {
    return (
        <BrowserRouter>
            <div className="min-h-screen bg-gray-100">
                <Routes>
                    {/* Giữ lại dòng chữ lúc nãy ở trang chủ cho đỡ trống */}
                    <Route path="/" element={<h1 className="p-4 text-2xl font-bold">Trang chủ LaptopStore</h1>} />

                    {/* Kích hoạt toàn bộ các đường dẫn của module Catalog */}
                    <Route path="/*" element={<CatalogRoutes />} />
                </Routes>
            </div>
        </BrowserRouter>
    );
}

export default App;