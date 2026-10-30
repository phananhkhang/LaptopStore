import React from 'react';
import { Outlet } from 'react-router-dom';
import { Header } from '../components/navigation/Header';
import { Footer } from '../components/navigation/Footer';

export const PublicLayout: React.FC = () => {
    return (
        <div className="min-h-screen flex flex-col bg-gray-50">
            <Header />

            {/* Phần main sẽ tự động co giãn đẩy Footer xuống đáy trang nhờ flex-grow */}
            <main className="flex-grow container mx-auto px-4 py-8">
                <Outlet />
            </main>

            <Footer />
        </div>
    );
};