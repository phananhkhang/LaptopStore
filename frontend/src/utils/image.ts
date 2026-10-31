/**
 * Chuẩn hóa URL ảnh trả về từ backend.
 *
 * Backend lưu đường dẫn tương đối dạng "/uploads/products/xxx.jpg" và phục vụ
 * tĩnh tại "/uploads/**". Khi dev, Vite proxy "/uploads" sang backend (:8081);
 * khi deploy, frontend và backend cùng origin (hoặc đặt VITE_ASSET_BASE_URL).
 *
 * Nhờ vậy ta không cần hardcode "http://localhost:8081" rải rác trong component.
 */
const ASSET_BASE_URL = (import.meta.env.VITE_ASSET_BASE_URL ?? '').replace(/\/+$/, '');

export const resolveImageUrl = (path?: string | null): string => {
  if (!path) return '';

  // Đã là URL tuyệt đối (http/https) hoặc data URI thì giữ nguyên.
  if (/^(https?:)?\/\//i.test(path) || path.startsWith('data:')) {
    return path;
  }

  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  return `${ASSET_BASE_URL}${normalizedPath}`;
};
