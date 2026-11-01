import { env } from '@/config/env';
import axios from 'axios';
import { getAccessToken } from './apiClient';

// Client cho API catalog công khai, trả thẳng response.data.
// Dùng cùng baseURL tương đối với apiClient để đi qua Vite proxy (không phụ thuộc CORS/host cứng).
const axiosClient = axios.create({
    baseURL: env.apiBaseUrl,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,
});

axiosClient.interceptors.request.use(
    (config) => {
        // Token đăng nhập được giữ trong bộ nhớ bởi apiClient (không lưu localStorage).
        const token = getAccessToken();
        if (token && config.headers) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

axiosClient.interceptors.response.use(
    (response) => response.data,
    (error) => Promise.reject(error)
);

export default axiosClient;
