-- создаём таблицу payments
CREATE TABLE IF NOT EXISTS payments (
    payment_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    order_id UUID,
    products_total DECIMAL(10, 2),
    delivery_total DECIMAL(10, 2),
    fee_total DECIMAL(10, 2),
    payment_state VARCHAR
    );
