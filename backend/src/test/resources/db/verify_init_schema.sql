-- Run only on an isolated test database after Flyway V1, with psql -v ON_ERROR_STOP=1.
-- Test rows and the temporary helper are rolled back; sequence values may advance.
BEGIN;

CREATE FUNCTION pg_temp.expect_sqlstate(command TEXT, expected_state TEXT)
RETURNS VOID LANGUAGE plpgsql AS $$
DECLARE
    actual_state TEXT;
BEGIN
    BEGIN
        EXECUTE command;
    EXCEPTION WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS actual_state = RETURNED_SQLSTATE;
        IF actual_state = expected_state THEN
            RETURN;
        END IF;
        RAISE;
    END;
    RAISE EXCEPTION 'Expected SQLSTATE %, but statement succeeded: %', expected_state, command;
END;
$$;

DO $$
DECLARE
    actual_count INTEGER;
    test_user BIGINT;
    test_brand BIGINT;
    test_category BIGINT;
    test_product BIGINT;
    test_variant BIGINT;
    test_cart BIGINT;
    test_order BIGINT;
    test_order_item BIGINT;
    test_payment BIGINT;
    test_coupon BIGINT;
    test_supplier BIGINT;
    test_purchase_order BIGINT;
BEGIN
    SELECT COUNT(*) INTO actual_count FROM pg_tables
    WHERE schemaname = 'public' AND tablename <> 'flyway_schema_history';
    IF actual_count <> 25 THEN RAISE EXCEPTION 'Expected 25 domain tables, got %', actual_count; END IF;

    SELECT COUNT(*) INTO actual_count FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name <> 'flyway_schema_history';
    IF actual_count <> 192 THEN RAISE EXCEPTION 'Expected 192 columns, got %', actual_count; END IF;

    SELECT COUNT(*) INTO actual_count FROM pg_constraint c
    JOIN pg_class t ON t.oid = c.conrelid JOIN pg_namespace n ON n.oid = t.relnamespace
    WHERE n.nspname = 'public' AND t.relname <> 'flyway_schema_history' AND c.contype = 'p';
    IF actual_count <> 25 THEN RAISE EXCEPTION 'Expected 25 PKs, got %', actual_count; END IF;

    SELECT COUNT(*) INTO actual_count FROM pg_constraint c
    JOIN pg_class t ON t.oid = c.conrelid JOIN pg_namespace n ON n.oid = t.relnamespace
    WHERE n.nspname = 'public' AND c.contype = 'f';
    IF actual_count <> 30 THEN RAISE EXCEPTION 'Expected 30 FKs, got %', actual_count; END IF;

    SELECT COUNT(*) INTO actual_count FROM pg_constraint c
    JOIN pg_class t ON t.oid = c.conrelid JOIN pg_namespace n ON n.oid = t.relnamespace
    WHERE n.nspname = 'public' AND c.contype = 'u';
    IF actual_count <> 14 THEN RAISE EXCEPTION 'Expected 14 UQs, got %', actual_count; END IF;

    IF (SELECT ARRAY_AGG(name ORDER BY name) FROM roles)
        IS DISTINCT FROM ARRAY['ROLE_ADMIN', 'ROLE_STAFF', 'ROLE_USER']::VARCHAR[] THEN
        RAISE EXCEPTION 'Expected exactly the three baseline roles';
    END IF;

    INSERT INTO users(email, password_hash) VALUES ('migration-test@example.invalid', 'fixture-hash')
        RETURNING id INTO test_user;
    INSERT INTO user_roles(user_id, role_id)
        SELECT test_user, id FROM roles WHERE name IN ('ROLE_USER', 'ROLE_STAFF');
    INSERT INTO users(email, password_hash) VALUES ('migration-test-2@example.invalid', 'fixture-hash');
    INSERT INTO user_roles(user_id, role_id)
        SELECT u.id, r.id FROM users u CROSS JOIN roles r
        WHERE u.email = 'migration-test-2@example.invalid' AND r.name = 'ROLE_USER';
    INSERT INTO addresses(user_id, recipient_name, phone, city, street)
        VALUES (test_user, 'Test recipient', '0900000000', 'Test city', 'Test street');
    INSERT INTO refresh_tokens(user_id, token_hash, family_id, expires_at)
        VALUES (test_user, 'fixture-token-hash', '00000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP + INTERVAL '1 day');

    INSERT INTO brands(name) VALUES ('Migration test brand') RETURNING id INTO test_brand;
    INSERT INTO categories(name) VALUES ('Migration test category') RETURNING id INTO test_category;
    INSERT INTO products(brand_id, category_id, name)
        VALUES (test_brand, test_category, 'Test laptop') RETURNING id INTO test_product;
    INSERT INTO product_variants(product_id, sku, price, quantity)
        VALUES (test_product, 'MIGRATION-TEST-SKU', 100, 5) RETURNING id INTO test_variant;
    INSERT INTO product_images(product_id, image_url) VALUES (test_product, '/fixture.png');
    -- Both rows must be permitted: product_id has no UQ in the ERD.
    INSERT INTO product_specifications(product_id, cpu)
        VALUES (test_product, 'CPU A'), (test_product, 'CPU B');

    INSERT INTO carts(user_id) VALUES (test_user) RETURNING id INTO test_cart;
    -- The ERD does not declare a cart/variant UQ; the service owns merging.
    INSERT INTO cart_items(cart_id, variant_id, quantity)
        VALUES (test_cart, test_variant, 1), (test_cart, test_variant, 2);

    INSERT INTO orders(user_id, order_code, status, subtotal, discount_amount, shipping_fee,
                       total_amount, recipient_name, recipient_phone, shipping_address, idempotency_key)
        VALUES (test_user, 'MIGRATION-TEST-ORDER', 'DELIVERED', 200, 10, 5,
                195, 'Test recipient', '0900000000', 'Test snapshot', 'migration-test-idempotency')
        RETURNING id INTO test_order;
    INSERT INTO order_items(order_id, product_id, variant_id, product_name, variant_name, sku, unit_price, quantity)
        VALUES (test_order, test_product, test_variant, 'Snapshot laptop', 'Snapshot variant', 'MIGRATION-TEST-SKU', 100, 2)
        RETURNING id INTO test_order_item;
    INSERT INTO order_status_history(order_id, from_status, to_status, changed_by_user_id)
        VALUES (test_order, 'SHIPPING', 'DELIVERED', test_user);
    -- Initial/system transitions may omit the previous state and user actor.
    INSERT INTO order_status_history(order_id, to_status) VALUES (test_order, 'PENDING');

    INSERT INTO payments(order_id, method, amount) VALUES (test_order, 'COD', 195)
        RETURNING id INTO test_payment;
    -- Provider transaction ID is not UQ in the baseline.
    INSERT INTO payment_transactions(payment_id, provider, provider_transaction_id, amount, status)
        VALUES (test_payment, 'TEST', 123, 195, 'PENDING'), (test_payment, 'TEST', 123, 195, 'SUCCESS');
    INSERT INTO coupons(code, discount_type, discount_value)
        VALUES ('MIGRATION-TEST-COUPON', 'FIXED', 10) RETURNING id INTO test_coupon;
    INSERT INTO coupon_usages(coupon_id, user_id, order_id, discount_amount, status)
        VALUES (test_coupon, test_user, test_order, 10, 'USED');
    INSERT INTO stock_movements(variant_id, type, delta, quantity_before, quantity_after,
                               reference_type, reference_id, operation_key, created_by_user_id)
        VALUES (test_variant, 'IMPORT', 5, 0, 5, 'TEST', 'fixture-reference', 'migration-test-movement', test_user);

    INSERT INTO suppliers(name) VALUES ('Migration test supplier') RETURNING id INTO test_supplier;
    INSERT INTO purchase_orders(supplier_id, created_by_user_id)
        VALUES (test_supplier, test_user) RETURNING id INTO test_purchase_order;
    INSERT INTO purchase_order_items(purchase_order_id, variant_id, quantity, received_quantity, unit_cost)
        VALUES (test_purchase_order, test_variant, 10, 5, 60), (test_purchase_order, test_variant, 10, 0, 60);
    INSERT INTO reviews(order_item_id, rating, comment) VALUES (test_order_item, 5, 'Test review');

    -- UNIQUE violations: SQLSTATE 23505.
    PERFORM pg_temp.expect_sqlstate(
        'INSERT INTO users(email, password_hash) VALUES (''migration-test@example.invalid'', ''fixture'')', '23505');
    PERFORM pg_temp.expect_sqlstate(format('INSERT INTO carts(user_id) VALUES (%s)', test_user), '23505');
    PERFORM pg_temp.expect_sqlstate(format(
        'INSERT INTO payments(order_id, method, amount) VALUES (%s, ''COD'', 195)', test_order), '23505');
    PERFORM pg_temp.expect_sqlstate(format(
        'INSERT INTO coupon_usages(coupon_id, user_id, order_id, discount_amount, status) VALUES (%s, %s, %s, 10, ''USED'')',
        test_coupon, test_user, test_order), '23505');
    PERFORM pg_temp.expect_sqlstate(format(
        'INSERT INTO reviews(order_item_id, rating) VALUES (%s, 4)', test_order_item), '23505');
    PERFORM pg_temp.expect_sqlstate(format(
        'INSERT INTO stock_movements(variant_id, type, delta, quantity_before, quantity_after, reference_type, reference_id, operation_key)
         VALUES (%s, ''IMPORT'', 5, 0, 5, ''TEST'', ''fixture'', ''migration-test-movement'')', test_variant), '23505');

    -- CHECK violations: SQLSTATE 23514.
    PERFORM pg_temp.expect_sqlstate(format('UPDATE product_variants SET quantity = -1 WHERE id = %s', test_variant), '23514');
    PERFORM pg_temp.expect_sqlstate(format('UPDATE cart_items SET quantity = 0 WHERE cart_id = %s', test_cart), '23514');
    PERFORM pg_temp.expect_sqlstate(format('UPDATE orders SET total_amount = 0 WHERE id = %s', test_order), '23514');
    PERFORM pg_temp.expect_sqlstate('UPDATE stock_movements SET quantity_after = 10 WHERE operation_key = ''migration-test-movement''', '23514');
    PERFORM pg_temp.expect_sqlstate(format(
        'UPDATE purchase_order_items SET received_quantity = quantity + 1 WHERE purchase_order_id = %s', test_purchase_order), '23514');
    PERFORM pg_temp.expect_sqlstate(format('UPDATE reviews SET rating = 6 WHERE order_item_id = %s', test_order_item), '23514');
    PERFORM pg_temp.expect_sqlstate(format(
        'UPDATE coupons SET starts_at = ''2026-10-02'', ends_at = ''2026-10-01'' WHERE id = %s', test_coupon), '23514');

    -- FK violations: SQLSTATE 23503. A referenced historical SKU cannot be deleted.
    PERFORM pg_temp.expect_sqlstate(format(
        'INSERT INTO cart_items(cart_id, variant_id, quantity) VALUES (%s, -1, 1)', test_cart), '23503');
    PERFORM pg_temp.expect_sqlstate(format('DELETE FROM product_variants WHERE id = %s', test_variant), '23503');

    RAISE NOTICE 'PASS: schema/role seed; inserts into all 25 tables; 15 rejected writes; ERD multiplicities preserved';
END;
$$;

ROLLBACK;
