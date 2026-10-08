-- Initial schema for a NEW PostgreSQL database.
-- Baseline: docs/architecture/11-entity-model.md and 12-ERD-LaptopStore.md.
-- 25 tables, 192 columns, 30 foreign keys and 14 unique constraints besides PKs.
-- A database with the former identity-only V1 needs a separate upgrade migration;
-- replacing an applied V1 changes its Flyway checksum.
-- CHECKs, defaults, indexes, SQL types and delete policies are implementation choices.

-- Identity and access
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150),
    phone VARCHAR(20),
    token_version INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT ck_users_token_version CHECK (token_version >= 0)
);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT uq_roles_name UNIQUE (name)
);

INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_STAFF'), ('ROLE_ADMIN');

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    recipient_name VARCHAR(150) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    ward VARCHAR(100),
    district VARCHAR(100),
    city VARCHAR(100) NOT NULL,
    street VARCHAR(255) NOT NULL,
    postal_code VARCHAR(20),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    family_id UUID NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Catalog
CREATE TABLE brands (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    logo_url VARCHAR(2048),
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_brands_name UNIQUE (name)
);

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_categories_name UNIQUE (name)
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    brand_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_brand FOREIGN KEY (brand_id) REFERENCES brands(id),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE product_variants (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    ram VARCHAR(255),
    storage VARCHAR(255),
    color VARCHAR(255),
    price NUMERIC(19,2) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    sku VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_product_variants_sku UNIQUE (sku),
    CONSTRAINT fk_product_variants_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT ck_product_variants_price CHECK (price >= 0),
    CONSTRAINT ck_product_variants_quantity CHECK (quantity >= 0)
);

CREATE TABLE product_images (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(2048) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- product_id is FK only: the ERD permits multiple specifications per product.
CREATE TABLE product_specifications (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    cpu VARCHAR(255),
    gpu VARCHAR(255),
    screen_size VARCHAR(255),
    screen_resolution VARCHAR(255),
    refresh_rate VARCHAR(255),
    battery VARCHAR(255),
    weight VARCHAR(255),
    os VARCHAR(255),
    CONSTRAINT fk_product_specifications_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Cart
CREATE TABLE carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_carts_user UNIQUE (user_id),
    CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- No additional UNIQUE(cart_id, variant_id): merge duplicate variants in the use case.
CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id),
    CONSTRAINT ck_cart_items_quantity CHECK (quantity > 0)
);

-- Ordering
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_code VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    subtotal NUMERIC(19,2) NOT NULL,
    discount_amount NUMERIC(19,2) NOT NULL DEFAULT 0,
    shipping_fee NUMERIC(19,2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(19,2) NOT NULL,
    recipient_name VARCHAR(255) NOT NULL,
    recipient_phone VARCHAR(20) NOT NULL,
    shipping_address TEXT NOT NULL,
    coupon_code_snapshot VARCHAR(100),
    idempotency_key VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_orders_order_code UNIQUE (order_code),
    CONSTRAINT uq_orders_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT ck_orders_amounts CHECK (
        subtotal >= 0 AND discount_amount >= 0 AND discount_amount <= subtotal
        AND shipping_fee >= 0 AND total_amount >= 0
    ),
    CONSTRAINT ck_orders_total CHECK (total_amount = subtotal - discount_amount + shipping_fee)
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    variant_name VARCHAR(255) NOT NULL,
    sku VARCHAR(255) NOT NULL,
    unit_price NUMERIC(19,2) NOT NULL,
    quantity INTEGER NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_order_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id),
    CONSTRAINT ck_order_items_unit_price CHECK (unit_price >= 0),
    CONSTRAINT ck_order_items_quantity CHECK (quantity > 0)
);

CREATE TABLE order_status_history (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by_user_id BIGINT,
    CONSTRAINT fk_order_status_history_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_status_history_user FOREIGN KEY (changed_by_user_id) REFERENCES users(id)
);

-- Payment
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    method VARCHAR(30) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ,
    CONSTRAINT uq_payments_order UNIQUE (order_id),
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT ck_payments_amount CHECK (amount >= 0)
);

CREATE TABLE payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    provider VARCHAR(50),
    provider_transaction_id BIGINT NOT NULL,
    provider_reference VARCHAR(100),
    amount NUMERIC(19,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_transactions_payment FOREIGN KEY (payment_id) REFERENCES payments(id),
    CONSTRAINT ck_payment_transactions_amount CHECK (amount >= 0)
);

-- Promotion
CREATE TABLE coupons (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL,
    discount_type VARCHAR(30) NOT NULL,
    discount_value NUMERIC(19,2) NOT NULL,
    max_discount NUMERIC(19,2),
    min_order_amount NUMERIC(19,2),
    usage_limit INTEGER,
    usage_per_user INTEGER,
    starts_at TIMESTAMPTZ,
    ends_at TIMESTAMPTZ,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_coupons_code UNIQUE (code),
    CONSTRAINT ck_coupons_discount CHECK (
        discount_value >= 0 AND (max_discount IS NULL OR max_discount >= 0)
        AND (min_order_amount IS NULL OR min_order_amount >= 0)
    ),
    CONSTRAINT ck_coupons_usage_limits CHECK (
        (usage_limit IS NULL OR usage_limit >= 0)
        AND (usage_per_user IS NULL OR usage_per_user >= 0)
    ),
    CONSTRAINT ck_coupons_period CHECK (starts_at IS NULL OR ends_at IS NULL OR starts_at < ends_at)
);

CREATE TABLE coupon_usages (
    id BIGSERIAL PRIMARY KEY,
    coupon_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    discount_amount NUMERIC(19,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_coupon_usages_order UNIQUE (order_id),
    CONSTRAINT fk_coupon_usages_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(id),
    CONSTRAINT fk_coupon_usages_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_coupon_usages_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT ck_coupon_usages_discount CHECK (discount_amount >= 0)
);

-- Inventory: current stock is product_variants.quantity, not a separate table.
CREATE TABLE stock_movements (
    id BIGSERIAL PRIMARY KEY,
    variant_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    delta INTEGER NOT NULL,
    quantity_before INTEGER NOT NULL,
    quantity_after INTEGER NOT NULL,
    reference_type VARCHAR(50) NOT NULL,
    reference_id VARCHAR(255) NOT NULL,
    operation_key VARCHAR(255) NOT NULL,
    note TEXT,
    created_by_user_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_stock_movements_operation_key UNIQUE (operation_key),
    CONSTRAINT fk_stock_movements_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id),
    CONSTRAINT fk_stock_movements_user FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT ck_stock_movements_quantities CHECK (quantity_before >= 0 AND quantity_after >= 0),
    CONSTRAINT ck_stock_movements_delta CHECK (
        quantity_after::BIGINT = quantity_before::BIGINT + delta::BIGINT
    )
);

-- Procurement
CREATE TABLE suppliers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(255),
    address TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE purchase_orders (
    id BIGSERIAL PRIMARY KEY,
    supplier_id BIGINT NOT NULL,
    created_by_user_id BIGINT,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    received_at TIMESTAMP,
    CONSTRAINT fk_purchase_orders_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    CONSTRAINT fk_purchase_orders_user FOREIGN KEY (created_by_user_id) REFERENCES users(id)
);

CREATE TABLE purchase_order_items (
    id BIGSERIAL PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    received_quantity INTEGER NOT NULL DEFAULT 0,
    unit_cost NUMERIC(19,2) NOT NULL,
    CONSTRAINT fk_purchase_order_items_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_purchase_order_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id),
    CONSTRAINT ck_purchase_order_items_quantity CHECK (quantity > 0),
    CONSTRAINT ck_purchase_order_items_received CHECK (received_quantity >= 0 AND received_quantity <= quantity),
    CONSTRAINT ck_purchase_order_items_unit_cost CHECK (unit_cost >= 0)
);

-- Review
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    order_item_id BIGINT NOT NULL,
    rating INTEGER NOT NULL,
    comment TEXT,
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_reviews_order_item UNIQUE (order_item_id),
    CONSTRAINT fk_reviews_order_item FOREIGN KEY (order_item_id) REFERENCES order_items(id),
    CONSTRAINT ck_reviews_rating CHECK (rating BETWEEN 1 AND 5)
);

-- FK/query indexes; existing PK/UQ indexes already cover their leading columns.
CREATE INDEX idx_user_roles_role ON user_roles(role_id);
CREATE INDEX idx_addresses_user ON addresses(user_id);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_family ON refresh_tokens(family_id);
CREATE INDEX idx_products_brand ON products(brand_id);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_product_variants_product ON product_variants(product_id);
CREATE INDEX idx_product_images_product ON product_images(product_id);
CREATE INDEX idx_product_specifications_product ON product_specifications(product_id);
CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);
CREATE INDEX idx_cart_items_variant ON cart_items(variant_id);
CREATE INDEX idx_orders_user_created ON orders(user_id, created_at DESC);
CREATE INDEX idx_orders_status_created ON orders(status, created_at DESC);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);
CREATE INDEX idx_order_items_variant ON order_items(variant_id);
CREATE INDEX idx_order_status_history_order_changed ON order_status_history(order_id, changed_at);
CREATE INDEX idx_order_status_history_user ON order_status_history(changed_by_user_id);
CREATE INDEX idx_payment_transactions_payment_created ON payment_transactions(payment_id, created_at);
CREATE INDEX idx_payment_transactions_provider_reference ON payment_transactions(provider, provider_reference);
CREATE INDEX idx_coupon_usages_coupon_status ON coupon_usages(coupon_id, status);
CREATE INDEX idx_coupon_usages_user_coupon_status ON coupon_usages(user_id, coupon_id, status);
CREATE INDEX idx_stock_movements_variant_created ON stock_movements(variant_id, created_at DESC);
CREATE INDEX idx_stock_movements_user ON stock_movements(created_by_user_id);
CREATE INDEX idx_purchase_orders_supplier ON purchase_orders(supplier_id);
CREATE INDEX idx_purchase_orders_user ON purchase_orders(created_by_user_id);
CREATE INDEX idx_purchase_order_items_order ON purchase_order_items(purchase_order_id);
CREATE INDEX idx_purchase_order_items_variant ON purchase_order_items(variant_id);
