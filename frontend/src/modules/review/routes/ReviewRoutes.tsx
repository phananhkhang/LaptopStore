import StaffReviewPage from '../pages/admin/StaffReviewPage';
import WriteReviewPage from '../pages/user/WriteReviewPage';
import ProductReviewsPage from '../pages/user/ProductReviewsPage';

// Mỗi trang review thuộc một vùng phân quyền khác nhau nên được gắn riêng trong AppRouter:
// - /products/:productId/reviews          công khai
// - /me/order-items/:orderItemId/review   USER đã đăng nhập
// - /staff/reviews                        STAFF & ADMIN
export { StaffReviewPage, WriteReviewPage, ProductReviewsPage };
