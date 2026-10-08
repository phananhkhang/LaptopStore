import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'; // Thêm Navigate vào đây
import { CatalogRoutes } from './modules/catalog/routes/CatalogRoutes';

function App() {
    return (
        <BrowserRouter>
            <div className="min-h-screen bg-gray-100">
                <Routes>
                    {/* Sửa lại chỗ này: Khi vào "/" sẽ tự động điều hướng sang "/products" */}
                    <Route path="/" element={<Navigate to="/products" replace />} />

                    <Route path="/*" element={<CatalogRoutes />} />
                </Routes>
            </div>
        </BrowserRouter>
    );
}

export default App;