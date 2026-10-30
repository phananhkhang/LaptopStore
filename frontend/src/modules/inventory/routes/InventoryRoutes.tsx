// @ts-ignore
import { Route, Routes } from 'react-router-dom';
import InventoryPage from '../pages/admin/InventoryPage';
import StockMovementHistoryPage from '../pages/admin/StockMovementHistoryPage';

export const InventoryRoutes = () => {
    return (
        <Routes>
            <Route path="/admin/inventory" element={<InventoryPage />} />
            <Route path="/admin/inventory/history" element={<StockMovementHistoryPage />} />
        </Routes>
    );
};