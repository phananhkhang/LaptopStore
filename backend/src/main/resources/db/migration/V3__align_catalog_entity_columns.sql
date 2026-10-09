-- Align the migrated schema with the existing catalog entity mappings.
-- Keep V1/V2 checksums unchanged for databases that have already applied them.
ALTER TABLE brands ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE brands ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE products ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE product_variants ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
