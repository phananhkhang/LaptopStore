-- Dữ liệu catalog DEMO cho môi trường phát triển. KHÔNG dùng cho production.
-- Chỉ chạy khi bật: FLYWAY_LOCATIONS=classpath:db/migration,classpath:db/seed
--
-- Script idempotent (Flyway repeatable migration, chạy lại khi nội dung đổi):
--   * Chỉ thêm brand/category/product chưa có (so khớp theo tên).
--   * Chỉ thêm biến thể chưa có SKU; tồn kho ban đầu được ghi kèm stock_movements.
--   * Chỉ gắn ảnh/thông số cho sản phẩm chưa có ảnh/thông số. Ảnh nằm ở backend/uploads/products.

CREATE TEMP TABLE seed_product (
    name              VARCHAR(255) PRIMARY KEY,
    brand             VARCHAR(255) NOT NULL,
    category          VARCHAR(255) NOT NULL,
    description       TEXT         NOT NULL,
    image_url         VARCHAR(2048) NOT NULL,
    cpu               VARCHAR(255),
    gpu               VARCHAR(255),
    screen_size       VARCHAR(255),
    screen_resolution VARCHAR(255),
    refresh_rate      VARCHAR(255),
    battery           VARCHAR(255),
    weight            VARCHAR(255),
    os                VARCHAR(255)
) ON COMMIT DROP;

INSERT INTO seed_product (name, brand, category, description, image_url, cpu, gpu, screen_size, screen_resolution, refresh_rate, battery, weight, os) VALUES
('Dell Inspiron 14 5430', 'Dell', 'Văn phòng', 'Thiết kế gọn, màn hình 16:10 dễ chịu cho học tập và làm việc cả ngày.', '/uploads/products/product-01.jpg', 'Intel Core i5-1340P', 'Intel Iris Xe', '14 inch', '1920 x 1200', '60Hz', '54Wh', '1.54kg', 'Windows 11'),
('Dell Latitude 5440', 'Dell', 'Văn phòng', 'Laptop doanh nghiệp bền bỉ, bảo mật tốt và bàn phím gõ thoải mái.', '/uploads/products/product-02.jpg', 'Intel Core i7-1355U', 'Intel Iris Xe', '14 inch', '1920 x 1080', '60Hz', '54Wh', '1.39kg', 'Windows 11 Pro'),
('Dell G15 5530', 'Dell', 'Gaming', 'Hiệu năng mạnh cho game và sáng tạo, tản nhiệt lớn, màn hình 165Hz.', '/uploads/products/product-03.png', 'Intel Core i7-13650HX', 'NVIDIA GeForce RTX 4060 8GB', '15.6 inch', '1920 x 1080', '165Hz', '86Wh', '2.65kg', 'Windows 11'),
('Dell XPS 15 9530', 'Dell', 'Đồ họa', 'Vỏ nhôm cao cấp, màn hình OLED 3.5K chuẩn màu cho dựng phim và thiết kế.', '/uploads/products/product-04.jpg', 'Intel Core i7-13700H', 'NVIDIA GeForce RTX 4050 6GB', '15.6 inch', '3456 x 2160 OLED', '60Hz', '86Wh', '1.86kg', 'Windows 11'),
('Dell Vostro 14 3430', 'Dell', 'Văn phòng', 'Lựa chọn tiết kiệm cho văn phòng và sinh viên, đủ cổng kết nối.', '/uploads/products/product-05.png', 'Intel Core i5-1335U', 'Intel Iris Xe', '14 inch', '1920 x 1080', '60Hz', '41Wh', '1.48kg', 'Windows 11'),
('HP Pavilion 14', 'HP', 'Mỏng nhẹ', 'Nhỏ gọn, sạc nhanh, phù hợp mang theo mỗi ngày.', '/uploads/products/product-06.jpg', 'Intel Core i5-1335U', 'Intel Iris Xe', '14 inch', '1920 x 1080', '60Hz', '43Wh', '1.41kg', 'Windows 11'),
('HP ProBook 440 G10', 'HP', 'Văn phòng', 'Độ bền chuẩn doanh nghiệp, webcam sắc nét cho họp trực tuyến.', '/uploads/products/product-07.jpg', 'Intel Core i5-1335U', 'Intel Iris Xe', '14 inch', '1920 x 1080', '60Hz', '51Wh', '1.38kg', 'Windows 11 Pro'),
('HP Victus 16', 'HP', 'Gaming', 'Màn hình lớn 144Hz, cấu hình cân bằng cho game thủ mới.', '/uploads/products/product-08.jpg', 'Intel Core i5-13500H', 'NVIDIA GeForce RTX 4050 6GB', '16.1 inch', '1920 x 1080', '144Hz', '70Wh', '2.29kg', 'Windows 11'),
('HP Envy 16', 'HP', 'Đồ họa', 'Màn hình OLED 2.8K 120Hz và GPU rời cho người làm sáng tạo.', '/uploads/products/product-09.jpg', 'Intel Core i7-13700H', 'NVIDIA GeForce RTX 4060 8GB', '16 inch', '2880 x 1800 OLED', '120Hz', '83Wh', '2.30kg', 'Windows 11'),
('HP Laptop 15s', 'HP', 'Văn phòng', 'Máy cơ bản giá tốt cho học online và tác vụ văn phòng.', '/uploads/products/product-10.jpg', 'Intel Core i3-1215U', 'Intel UHD Graphics', '15.6 inch', '1920 x 1080', '60Hz', '41Wh', '1.69kg', 'Windows 11'),
('Lenovo ThinkPad E14 Gen 5', 'Lenovo', 'Văn phòng', 'Bàn phím ThinkPad nổi tiếng, bền bỉ cho công việc chuyên nghiệp.', '/uploads/products/product-11.jpg', 'Intel Core i5-1335U', 'Intel Iris Xe', '14 inch', '1920 x 1200', '60Hz', '47Wh', '1.41kg', 'Windows 11 Pro'),
('Lenovo IdeaPad Slim 5 14', 'Lenovo', 'Mỏng nhẹ', 'Mỏng nhẹ, màn hình OLED rực rỡ, pin dùng trọn ngày.', '/uploads/products/product-12.jpg', 'AMD Ryzen 7 7730U', 'AMD Radeon Graphics', '14 inch', '1920 x 1200 OLED', '60Hz', '57Wh', '1.46kg', 'Windows 11'),
('Lenovo LOQ 15', 'Lenovo', 'Gaming', 'Cấu hình cân bằng cho học lập trình, sáng tạo và chơi game.', '/uploads/products/product-13.jpg', 'Intel Core i5-12450HX', 'NVIDIA GeForce RTX 4050 6GB', '15.6 inch', '1920 x 1080', '144Hz', '60Wh', '2.38kg', 'Windows 11'),
('Lenovo Legion 5 16', 'Lenovo', 'Gaming', 'Màn hình 2.5K 165Hz, tản nhiệt Legion Coldfront cho hiệu năng ổn định.', '/uploads/products/product-14.webp', 'AMD Ryzen 7 7840HS', 'NVIDIA GeForce RTX 4060 8GB', '16 inch', '2560 x 1600', '165Hz', '80Wh', '2.40kg', 'Windows 11'),
('Lenovo Yoga Pro 7', 'Lenovo', 'Đồ họa', 'Màn hình 3K 120Hz sắc nét trong thân máy gọn nhẹ.', '/uploads/products/product-15.jpg', 'AMD Ryzen 7 7840HS', 'AMD Radeon 780M', '14.5 inch', '3072 x 1920', '120Hz', '73Wh', '1.49kg', 'Windows 11'),
('ASUS Vivobook 15 OLED', 'ASUS', 'Văn phòng', 'Màn hình OLED 15.6 inch rộng rãi, bàn phím số tiện lợi.', '/uploads/products/product-16.jpg', 'Intel Core i5-1335U', 'Intel Iris Xe', '15.6 inch', '1920 x 1080 OLED', '60Hz', '50Wh', '1.70kg', 'Windows 11'),
('ASUS Zenbook 14 OLED', 'ASUS', 'Mỏng nhẹ', 'Siêu nhẹ 1.2kg, OLED 3K 120Hz và pin 75Wh cho cả ngày dài.', '/uploads/products/product-17.jpg', 'Intel Core Ultra 7 155H', 'Intel Arc Graphics', '14 inch', '2880 x 1800 OLED', '120Hz', '75Wh', '1.20kg', 'Windows 11'),
('ASUS TUF Gaming A15', 'ASUS', 'Gaming', 'Bền chuẩn quân đội, pin lớn, sẵn sàng cho những trận game dài.', '/uploads/products/product-18.jpg', 'AMD Ryzen 7 7735HS', 'NVIDIA GeForce RTX 4050 6GB', '15.6 inch', '1920 x 1080', '144Hz', '90Wh', '2.20kg', 'Windows 11'),
('ASUS ROG Strix G16', 'ASUS', 'Gaming', 'Hiệu năng bứt phá, màn hình 165Hz và hệ thống tản nhiệt tối ưu.', '/uploads/products/product-19.jpg', 'Intel Core i7-13650HX', 'NVIDIA GeForce RTX 4060 8GB', '16 inch', '1920 x 1200', '165Hz', '90Wh', '2.50kg', 'Windows 11'),
('ASUS ProArt Studiobook 16', 'ASUS', 'Đồ họa', 'Máy trạm sáng tạo với OLED 3.2K và núm xoay ASUS Dial.', '/uploads/products/product-20.jpeg', 'Intel Core i7-13700H', 'NVIDIA GeForce RTX 4060 8GB', '16 inch', '3200 x 2000 OLED', '120Hz', '90Wh', '2.40kg', 'Windows 11 Pro'),
('Acer Aspire 5 15', 'Acer', 'Văn phòng', 'Laptop đa dụng giá hợp lý cho gia đình và văn phòng.', '/uploads/products/product-21.png', 'Intel Core i5-1335U', 'Intel Iris Xe', '15.6 inch', '1920 x 1080', '60Hz', '50Wh', '1.76kg', 'Windows 11'),
('Acer Swift Go 14 OLED', 'Acer', 'Mỏng nhẹ', 'Vỏ nhôm mỏng nhẹ, màn hình OLED 2.8K cho người hay di chuyển.', '/uploads/products/product-22.jpg', 'Intel Core Ultra 5 125H', 'Intel Arc Graphics', '14 inch', '2880 x 1800 OLED', '90Hz', '65Wh', '1.32kg', 'Windows 11'),
('Acer Nitro V 15', 'Acer', 'Gaming', 'Laptop gaming dễ tiếp cận, màn hình 144Hz mượt mà.', '/uploads/products/product-23.jpg', 'Intel Core i5-13420H', 'NVIDIA GeForce RTX 4050 6GB', '15.6 inch', '1920 x 1080', '144Hz', '57Wh', '2.10kg', 'Windows 11'),
('Acer Predator Helios Neo 16', 'Acer', 'Gaming', 'Cấu hình mạnh, màn hình 16 inch 165Hz cho game thủ chuyên nghiệp.', '/uploads/products/product-24.png', 'Intel Core i7-13700HX', 'NVIDIA GeForce RTX 4060 8GB', '16 inch', '1920 x 1200', '165Hz', '90Wh', '2.60kg', 'Windows 11'),
('Acer TravelMate P2', 'Acer', 'Văn phòng', 'Laptop doanh nghiệp gọn nhẹ, bảo mật vân tay.', '/uploads/products/product-25.jpg', 'Intel Core i5-1335U', 'Intel Iris Xe', '14 inch', '1920 x 1080', '60Hz', '50Wh', '1.50kg', 'Windows 11 Pro'),
('MacBook Air 13 M2', 'Apple', 'Mỏng nhẹ', 'Thiết kế mỏng không quạt, pin bền bỉ cho học tập và công việc.', '/uploads/products/product-26.jpg', 'Apple M2 8 nhân CPU', 'GPU 8 nhân', '13.6 inch', '2560 x 1664', '60Hz', '52.6Wh', '1.24kg', 'macOS'),
('MacBook Air 15 M3', 'Apple', 'Mỏng nhẹ', 'Màn hình 15 inch rộng rãi trong thân máy siêu mỏng.', '/uploads/products/product-27.jpg', 'Apple M3 8 nhân CPU', 'GPU 10 nhân', '15.3 inch', '2880 x 1864', '60Hz', '66.5Wh', '1.51kg', 'macOS'),
('MacBook Pro 14 M3 Pro', 'Apple', 'Đồ họa', 'Màn hình Liquid Retina XDR 120Hz, hiệu năng chuyên nghiệp.', '/uploads/products/product-28.jpg', 'Apple M3 Pro 11 nhân CPU', 'GPU 14 nhân', '14.2 inch', '3024 x 1964', '120Hz', '72.4Wh', '1.61kg', 'macOS'),
('MacBook Pro 16 M3 Pro', 'Apple', 'Đồ họa', 'Màn hình lớn 16 inch và pin 100Wh cho dự án nặng.', '/uploads/products/product-29.png', 'Apple M3 Pro 12 nhân CPU', 'GPU 18 nhân', '16.2 inch', '3456 x 2234', '120Hz', '100Wh', '2.14kg', 'macOS'),
('MacBook Air 13 M3', 'Apple', 'Mỏng nhẹ', 'Chip M3 mạnh mẽ, nhẹ 1.24kg, sẵn sàng cho mọi hành trình.', '/uploads/products/product-30.jpg', 'Apple M3 8 nhân CPU', 'GPU 8 nhân', '13.6 inch', '2560 x 1664', '60Hz', '52.6Wh', '1.24kg', 'macOS');

CREATE TEMP TABLE seed_variant (
    product_name VARCHAR(255) NOT NULL,
    sku          VARCHAR(255) PRIMARY KEY,
    ram          VARCHAR(255) NOT NULL,
    storage      VARCHAR(255) NOT NULL,
    color        VARCHAR(255) NOT NULL,
    price        NUMERIC(19,2) NOT NULL,
    quantity     INTEGER NOT NULL
) ON COMMIT DROP;

INSERT INTO seed_variant (product_name, sku, ram, storage, color, price, quantity) VALUES
('Dell Inspiron 14 5430', 'DEL-INS14-16-512', '16GB', '512GB SSD', 'Bạc', 18990000, 12),
('Dell Latitude 5440', 'DEL-LAT5440-16-512', '16GB', '512GB SSD', 'Xám', 24490000, 8),
('Dell G15 5530', 'DEL-G15-16-512', '16GB', '512GB SSD', 'Đen', 32990000, 6),
('Dell G15 5530', 'DEL-G15-32-1TB', '32GB', '1TB SSD', 'Đen', 37490000, 3),
('Dell XPS 15 9530', 'DEL-XPS15-16-1TB', '16GB', '1TB SSD', 'Bạc', 52990000, 4),
('Dell Vostro 14 3430', 'DEL-VOS14-8-512', '8GB', '512GB SSD', 'Xám', 15490000, 15),
('HP Pavilion 14', 'HP-PAV14-16-512', '16GB', '512GB SSD', 'Bạc', 17490000, 10),
('HP ProBook 440 G10', 'HP-PB440-16-512', '16GB', '512GB SSD', 'Bạc', 19990000, 9),
('HP Victus 16', 'HP-VIC16-16-512', '16GB', '512GB SSD', 'Đen', 25990000, 7),
('HP Envy 16', 'HP-ENVY16-16-1TB', '16GB', '1TB SSD', 'Bạc', 42990000, 3),
('HP Laptop 15s', 'HP-15S-8-256', '8GB', '256GB SSD', 'Bạc', 10990000, 20),
('Lenovo ThinkPad E14 Gen 5', 'LEN-E14G5-16-512', '16GB', '512GB SSD', 'Đen', 21990000, 8),
('Lenovo IdeaPad Slim 5 14', 'LEN-SLIM5-16-512', '16GB', '512GB SSD', 'Xám', 18490000, 11),
('Lenovo LOQ 15', 'LEN-LOQ15-16-512', '16GB', '512GB SSD', 'Xám', 22990000, 10),
('Lenovo Legion 5 16', 'LEN-LEG5-16-1TB', '16GB', '1TB SSD', 'Xám', 36990000, 5),
('Lenovo Legion 5 16', 'LEN-LEG5-32-1TB', '32GB', '1TB SSD', 'Xám', 41990000, 2),
('Lenovo Yoga Pro 7', 'LEN-YOGA7-16-1TB', '16GB', '1TB SSD', 'Xám', 29990000, 4),
('ASUS Vivobook 15 OLED', 'ASU-VIVO15-16-512', '16GB', '512GB SSD', 'Bạc', 16990000, 14),
('ASUS Zenbook 14 OLED', 'ASU-ZEN14-16-1TB', '16GB', '1TB SSD', 'Xanh', 27990000, 6),
('ASUS TUF Gaming A15', 'ASU-TUFA15-16-512', '16GB', '512GB SSD', 'Xám', 23490000, 9),
('ASUS ROG Strix G16', 'ASU-ROGG16-16-512', '16GB', '512GB SSD', 'Xám', 35990000, 5),
('ASUS ROG Strix G16', 'ASU-ROGG16-32-1TB', '32GB', '1TB SSD', 'Xám', 40990000, 3),
('ASUS ProArt Studiobook 16', 'ASU-PROART16-32-1TB', '32GB', '1TB SSD', 'Đen', 54990000, 2),
('Acer Aspire 5 15', 'ACE-ASP5-8-512', '8GB', '512GB SSD', 'Bạc', 13990000, 16),
('Acer Swift Go 14 OLED', 'ACE-SWGO14-16-512', '16GB', '512GB SSD', 'Bạc', 20990000, 7),
('Acer Nitro V 15', 'ACE-NITV15-16-512', '16GB', '512GB SSD', 'Đen', 21490000, 12),
('Acer Predator Helios Neo 16', 'ACE-HELNEO16-16-1TB', '16GB', '1TB SSD', 'Đen', 37990000, 4),
('Acer TravelMate P2', 'ACE-TMP2-16-512', '16GB', '512GB SSD', 'Đen', 17990000, 8),
('MacBook Air 13 M2', 'APL-MBA13M2-8-256', '8GB', '256GB SSD', 'Xám', 24990000, 10),
('MacBook Air 13 M2', 'APL-MBA13M2-16-512', '16GB', '512GB SSD', 'Xám', 31990000, 4),
('MacBook Air 15 M3', 'APL-MBA15M3-16-512', '16GB', '512GB SSD', 'Bạc', 36990000, 5),
('MacBook Pro 14 M3 Pro', 'APL-MBP14M3P-18-512', '18GB', '512GB SSD', 'Đen', 49990000, 3),
('MacBook Pro 16 M3 Pro', 'APL-MBP16M3P-18-512', '18GB', '512GB SSD', 'Đen', 62990000, 2),
('MacBook Air 13 M3', 'APL-MBA13M3-16-256', '16GB', '256GB SSD', 'Xanh đen', 27990000, 8);

-- 1. Thương hiệu và danh mục (so khớp không phân biệt hoa thường để dùng lại dữ liệu sẵn có)
INSERT INTO brands (name, description, active)
SELECT DISTINCT s.brand, 'Thương hiệu ' || s.brand, TRUE
FROM seed_product s
WHERE NOT EXISTS (SELECT 1 FROM brands b WHERE lower(b.name) = lower(s.brand));

INSERT INTO categories (name, description, active)
SELECT c.name, c.description, TRUE
FROM (VALUES
    ('Gaming', 'Hiệu năng cho game và sáng tạo.'),
    ('Văn phòng', 'Học tập và làm việc mỗi ngày.'),
    ('Mỏng nhẹ', 'Linh hoạt trên mọi hành trình.'),
    ('Đồ họa', 'Màn hình chuẩn màu, GPU mạnh cho thiết kế và dựng phim.')
) AS c(name, description)
WHERE NOT EXISTS (SELECT 1 FROM categories x WHERE lower(x.name) = lower(c.name));

-- 2. Sản phẩm
INSERT INTO products (brand_id, category_id, name, description, status, active)
SELECT b.id, c.id, s.name, s.description, 'ACTIVE', TRUE
FROM seed_product s
JOIN LATERAL (SELECT id FROM brands WHERE lower(name) = lower(s.brand) ORDER BY id LIMIT 1) b ON TRUE
JOIN LATERAL (SELECT id FROM categories WHERE lower(name) = lower(s.category) ORDER BY id LIMIT 1) c ON TRUE
WHERE NOT EXISTS (SELECT 1 FROM products p WHERE p.name = s.name);

-- 3. Thông số kỹ thuật
INSERT INTO product_specifications (product_id, cpu, gpu, screen_size, screen_resolution, refresh_rate, battery, weight, os)
SELECT p.id, s.cpu, s.gpu, s.screen_size, s.screen_resolution, s.refresh_rate, s.battery, s.weight, s.os
FROM seed_product s
JOIN LATERAL (SELECT id FROM products WHERE name = s.name ORDER BY id LIMIT 1) p ON TRUE
WHERE NOT EXISTS (SELECT 1 FROM product_specifications ps WHERE ps.product_id = p.id);

-- 4. Ảnh chính (file có sẵn trong backend/uploads/products)
INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, s.image_url, TRUE
FROM seed_product s
JOIN LATERAL (SELECT id FROM products WHERE name = s.name ORDER BY id LIMIT 1) p ON TRUE
WHERE NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

-- Hai sản phẩm mẫu cũ (LaptopStore-Docker.session.sql) chưa có ảnh: dùng ảnh cùng dòng máy.
INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, m.image_url, TRUE
FROM (VALUES
    ('ASUS ROG Strix G15', '/uploads/products/product-19.jpg'),
    ('Dell XPS 13', '/uploads/products/product-04.jpg')
) AS m(name, image_url)
JOIN products p ON p.name = m.name
WHERE NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

-- 5. Biến thể mới kèm bút toán tồn kho ban đầu (quantity luôn đi cùng stock_movements)
WITH inserted AS (
    INSERT INTO product_variants (product_id, sku, ram, storage, color, price, quantity, status, active)
    SELECT p.id, v.sku, v.ram, v.storage, v.color, v.price, v.quantity, 'ACTIVE', TRUE
    FROM seed_variant v
    JOIN LATERAL (SELECT id FROM products WHERE name = v.product_name ORDER BY id LIMIT 1) p ON TRUE
    ON CONFLICT (sku) DO NOTHING
    RETURNING id, sku, quantity
)
INSERT INTO stock_movements (variant_id, type, delta, quantity_before, quantity_after,
                             reference_type, reference_id, operation_key, note)
SELECT i.id, 'IN', i.quantity, 0, i.quantity,
       'DEMO_SEED', i.sku, 'demo-seed-' || i.sku, 'Tồn kho khởi tạo dữ liệu demo'
FROM inserted i
WHERE i.quantity > 0
ON CONFLICT (operation_key) DO NOTHING;

-- 6. Dữ liệu mẫu cũ dùng status 'AVAILABLE' nhưng giỏ hàng/checkout chỉ bán 'ACTIVE'.
UPDATE products SET status = 'ACTIVE' WHERE upper(status) = 'AVAILABLE';
UPDATE product_variants SET status = 'ACTIVE' WHERE upper(status) = 'AVAILABLE';
