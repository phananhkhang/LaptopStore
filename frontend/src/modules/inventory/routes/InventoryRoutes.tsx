// @ts-ignore
import { Route, Routes } from 'react-router-dom';
import InventoryDashboardPage from '../pages/admin/InventoryDashboardPage';

export const InventoryRoutes = () => {
    return (
        <Routes>
            <Route path="/admin/inventory" element={<InventoryDashboardPage />} />
        </Routes>
    );
};