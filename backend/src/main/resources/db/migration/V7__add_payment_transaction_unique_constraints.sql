CREATE UNIQUE INDEX IF NOT EXISTS
uq_payment_transactions_provider_reference
ON payment_transactions (
    provider,
    provider_reference
)
WHERE provider_reference IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS
uq_payment_transactions_provider_transaction_id
ON payment_transactions (
    provider_transaction_id
)
WHERE provider_transaction_id IS NOT NULL;