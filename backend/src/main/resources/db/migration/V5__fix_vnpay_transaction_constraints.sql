-- Khi vừa tạo payment attempt, VNPay chưa cấp transaction number.
ALTER TABLE payment_transactions
    ALTER COLUMN provider_transaction_id DROP NOT NULL;

-- Khi VNPay đã cấp transaction number thì transaction number phải unique.
CREATE UNIQUE INDEX IF NOT EXISTS uq_payment_transactions_provider_transaction_id
    ON payment_transactions(provider_transaction_id)
    WHERE provider_transaction_id IS NOT NULL;