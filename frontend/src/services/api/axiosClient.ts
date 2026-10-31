import axios from 'axios';

// Khởi tạo một instance của Axios với cấu hình mặc định
const axiosClient = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/api/v1',
    headers: {
        'Content-Type': 'application/json',
    },
});

// 1. Interceptor can thiệp vào REQUEST (Trước khi gửi đi)
axiosClient.interceptors.request.use(
    (config) => {
        // Lấy token từ LocalStorage
        const token = localStorage.getItem('accessToken');
        if (token && config.headers) {
            // Đính kèm token vào header Authorization theo chuẩn Bearer
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// 2. Interceptor can thiệp vào RESPONSE (Sau khi nhận kết quả)
axiosClient.interceptors.response.use(
    (response) => {
        // Nếu API trả về thành công, chỉ lấy đúng data mình cần (tùy cấu trúc backend trả về)
        return response.data; 
    },
    (error) => {
        // Bắt lỗi chung toàn cục (Ví dụ: 401 Unauthorized do token hết hạn)
        if (error.response && error.response.status === 401) {
            console.error("Token không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại!");
            // localStorage.removeItem('accessToken');
            // window.location.href = '/login'; // Chuyển hướng về login nếu cần
        }
        return Promise.reject(error);
    }
);

export default axiosClient;