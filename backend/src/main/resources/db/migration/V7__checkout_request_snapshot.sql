-- Request metadata (not a projection of payment status).
ALTER TABLE orders ADD COLUMN checkout_payment_method VARCHAR(10) NOT NULL DEFAULT 'COD';
ALTER TABLE orders ADD COLUMN customer_note VARCHAR(500);
UPDATE orders o SET checkout_payment_method = 'VNPAY'
WHERE o.status = 'PENDING_PAYMENT' OR EXISTS (SELECT 1 FROM order_status_history h WHERE h.order_id=o.id AND h.to_status='PENDING_PAYMENT')
OR EXISTS (SELECT 1 FROM payments p WHERE p.order_id=o.id AND p.method='VNPAY');
ALTER TABLE orders DROP CONSTRAINT uq_orders_idempotency_key;
ALTER TABLE orders ADD CONSTRAINT uq_orders_user_idempotency UNIQUE(user_id, idempotency_key);
ALTER TABLE orders ADD CONSTRAINT ck_orders_checkout_method CHECK (checkout_payment_method IN ('COD','VNPAY'));
