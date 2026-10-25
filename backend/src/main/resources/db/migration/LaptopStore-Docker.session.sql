-- Thêm Categories
INSERT INTO categories (name, description, active) VALUES 
('Gaming', 'Laptop cấu hình cao chuyên chơi game', true),
('Office', 'Laptop mỏng nhẹ, văn phòng', true);

-- Thêm Brands
INSERT INTO brands (name, logo_url, description, active, created_at, updated_at) VALUES 
('ASUS', 'https://example.com/asus.png', 'Thương hiệu ASUS', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('DELL', 'https://example.com/dell.png', 'Thương hiệu DELL', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Thêm Products
INSERT INTO products (category_id, brand_id, name, description, status, active, created_at, updated_at) VALUES 
(1, 1, 'ASUS ROG Strix G15', 'Laptop Gaming ASUS cực mạnh', 'AVAILABLE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 2, 'Dell XPS 13', 'Laptop doanh nhân sang trọng', 'AVAILABLE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Thêm Product Variants
INSERT INTO product_variants (product_id, sku, ram, storage, color, price, quantity, status, active, created_at, updated_at) VALUES 
(1, 'ROG-G15-01', '16GB', '512GB SSD', 'Black', 25000000.00, 10, 'AVAILABLE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'ROG-G15-02', '32GB', '1TB SSD', 'Black', 30000000.00, 5, 'AVAILABLE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'XPS13-01', '16GB', '512GB SSD', 'Silver', 35000000.00, 8, 'AVAILABLE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);