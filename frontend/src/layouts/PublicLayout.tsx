import React from 'react';
import { Outlet } from 'react-router-dom';
import { Footer } from '../components/navigation/Footer';
import { Header } from '../components/navigation/Header';

export const PublicLayout: React.FC = () => (
    <div className="min-h-screen flex flex-col bg-white">
        <a href="#main" className="sr-only focus:not-sr-only focus:fixed focus:left-3 focus:top-3 focus:z-[200] focus:bg-primary focus:text-white focus:px-4 focus:py-2 focus:rounded-lg">
            Bỏ qua tới nội dung chính
        </a>
        <Header />
        <main id="main" className="flex-grow">
            <Outlet />
        </main>
        <Footer />
    </div>
);
