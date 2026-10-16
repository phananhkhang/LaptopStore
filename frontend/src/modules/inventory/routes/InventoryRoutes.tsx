// @ts-ignore
import { Route, Routes } from 'react-router-dom';
import InventoryDashboardPage from '../pages/admin/InventoryDashboardPage';
import StockMovementHistoryPage from '../pages/admin/StockMovementHistoryPage';

export const InventoryRoutes = () => {
    return (
        <Routes>
            <Route path="/admin/inventory" element={<InventoryDashboardPage />} />
            <Route path="/admin/inventory/history" element={<StockMovementHistoryPage />} />
        </Routes>
    );
};