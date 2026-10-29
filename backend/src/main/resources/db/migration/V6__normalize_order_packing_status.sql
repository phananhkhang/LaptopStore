-- Preserve orders and history created before the lifecycle vocabulary was unified.
UPDATE orders SET status = 'PACKING' WHERE status = 'PACKED';
UPDATE order_status_history SET from_status = 'PACKING' WHERE from_status = 'PACKED';
UPDATE order_status_history SET to_status = 'PACKING' WHERE to_status = 'PACKED';
