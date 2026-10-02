-- ==============================================================================
-- V2: Create Catalog Schema (brands, categories, products, etc.)
-- ==============================================================================

-- 1. brands
CREATE TABLE IF NOT EXISTS brands (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    logo_url VARCHAR(255),
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_brands_active ON brands(active);
CREATE INDEX IF NOT EXISTS idx_brands_name ON brands(name);

-- Seed sample brands
INSERT INTO brands (name, active) VALUES ('Dell', TRUE) ON CONFLICT (name) DO NOTHING;
INSERT INTO brands (name, active) VALUES ('HP', TRUE) ON CONFLICT (name) DO NOTHING;
INSERT INTO brands (name, active) VALUES ('Lenovo', TRUE) ON CONFLICT (name) DO NOTHING;
INSERT INTO brands (name, active) VALUES ('Asus', TRUE) ON CONFLICT (name) DO NOTHING;
INSERT INTO brands (name, active) VALUES ('Apple', TRUE) ON CONFLICT (name) DO NOTHING;
