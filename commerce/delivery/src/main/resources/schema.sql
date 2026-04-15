-- создаём таблицу delivery
CREATE TABLE IF NOT EXISTS delivery (
    delivery_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    volume DECIMAL(10, 2),
    weight DECIMAL(10, 2),
    is_fragile boolean NOT NULL,
    warehouse_address_name VARCHAR,
    from_country VARCHAR,
    from_city VARCHAR,
    from_street VARCHAR,
    from_house VARCHAR,
    from_flat VARCHAR,
    to_country VARCHAR,
    to_city VARCHAR,
    to_street VARCHAR,
    to_house VARCHAR,
    to_flat VARCHAR,
    delivery_state VARCHAR,
    order_id UUID
    );
