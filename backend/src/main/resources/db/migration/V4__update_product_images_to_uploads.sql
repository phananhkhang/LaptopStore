-- Update mock catalog product images to local files under /uploads/products/
-- Depends on V2__seed_catalog_mock_data.sql

UPDATE product_images pi
SET image_url = '/uploads/products/product-01.jpg', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'Dell Inspiron 14 5430';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-01.jpg', TRUE
FROM products p
WHERE p.name = 'Dell Inspiron 14 5430'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-02.jpg', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'Dell Latitude 5440';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-02.jpg', TRUE
FROM products p
WHERE p.name = 'Dell Latitude 5440'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-03.png', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'Dell G15 5530';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-03.png', TRUE
FROM products p
WHERE p.name = 'Dell G15 5530'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-04.jpg', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'Dell XPS 15 9530';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-04.jpg', TRUE
FROM products p
WHERE p.name = 'Dell XPS 15 9530'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-05.png', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'Dell Vostro 14 3430';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-05.png', TRUE
FROM products p
WHERE p.name = 'Dell Vostro 14 3430'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-06.jpg', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'HP Pavilion 14';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-06.jpg', TRUE
FROM products p
WHERE p.name = 'HP Pavilion 14'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-07.jpg', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'HP ProBook 440 G10';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-07.jpg', TRUE
FROM products p
WHERE p.name = 'HP ProBook 440 G10'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-08.jpg', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'HP Victus 16';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-08.jpg', TRUE
FROM products p
WHERE p.name = 'HP Victus 16'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-09.jpg', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'HP Envy 16';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-09.jpg', TRUE
FROM products p
WHERE p.name = 'HP Envy 16'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-10.jpg', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'HP Laptop 15s';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-10.jpg', TRUE
FROM products p
WHERE p.name = 'HP Laptop 15s'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-11.jpg', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'Lenovo ThinkPad E14 Gen 5';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-11.jpg', TRUE
FROM products p
WHERE p.name = 'Lenovo ThinkPad E14 Gen 5'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-12.jpg', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'Lenovo IdeaPad Slim 5 14';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-12.jpg', TRUE
FROM products p
WHERE p.name = 'Lenovo IdeaPad Slim 5 14'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-13.jpg', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'Lenovo LOQ 15';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-13.jpg', TRUE
FROM products p
WHERE p.name = 'Lenovo LOQ 15'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-14.webp', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'Lenovo Legion 5 16';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-14.webp', TRUE
FROM products p
WHERE p.name = 'Lenovo Legion 5 16'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-15.jpg', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'Lenovo Yoga Pro 7';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-15.jpg', TRUE
FROM products p
WHERE p.name = 'Lenovo Yoga Pro 7'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-16.jpg', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'ASUS Vivobook 15 OLED';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-16.jpg', TRUE
FROM products p
WHERE p.name = 'ASUS Vivobook 15 OLED'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-17.jpg', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'ASUS Zenbook 14 OLED';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-17.jpg', TRUE
FROM products p
WHERE p.name = 'ASUS Zenbook 14 OLED'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-18.jpg', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'ASUS TUF Gaming A15';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-18.jpg', TRUE
FROM products p
WHERE p.name = 'ASUS TUF Gaming A15'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-19.jpg', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'ASUS ROG Strix G16';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-19.jpg', TRUE
FROM products p
WHERE p.name = 'ASUS ROG Strix G16'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-20.jpeg', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'ASUS ProArt Studiobook 16';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-20.jpeg', TRUE
FROM products p
WHERE p.name = 'ASUS ProArt Studiobook 16'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-21.png', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'Acer Aspire 5 15';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-21.png', TRUE
FROM products p
WHERE p.name = 'Acer Aspire 5 15'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-22.jpg', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'Acer Swift Go 14 OLED';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-22.jpg', TRUE
FROM products p
WHERE p.name = 'Acer Swift Go 14 OLED'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-23.jpg', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'Acer Nitro V 15';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-23.jpg', TRUE
FROM products p
WHERE p.name = 'Acer Nitro V 15'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-24.png', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'Acer Predator Helios Neo 16';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-24.png', TRUE
FROM products p
WHERE p.name = 'Acer Predator Helios Neo 16'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-25.jpg', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'Acer TravelMate P2';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-25.jpg', TRUE
FROM products p
WHERE p.name = 'Acer TravelMate P2'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-26.jpg', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'MacBook Air 13 M2';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-26.jpg', TRUE
FROM products p
WHERE p.name = 'MacBook Air 13 M2'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-27.jpg', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'MacBook Air 15 M3';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-27.jpg', TRUE
FROM products p
WHERE p.name = 'MacBook Air 15 M3'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-28.jpg', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'MacBook Pro 14 M3 Pro';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-28.jpg', TRUE
FROM products p
WHERE p.name = 'MacBook Pro 14 M3 Pro'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-29.png', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'MacBook Pro 16 M3 Pro';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-29.png', TRUE
FROM products p
WHERE p.name = 'MacBook Pro 16 M3 Pro'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);

UPDATE product_images pi
SET image_url = '/uploads/products/product-30.jpg', is_primary = TRUE
FROM products p
WHERE pi.product_id = p.id AND p.name = 'MacBook Air 13 M3';

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, '/uploads/products/product-30.jpg', TRUE
FROM products p
WHERE p.name = 'MacBook Air 13 M3'
  AND NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id);
