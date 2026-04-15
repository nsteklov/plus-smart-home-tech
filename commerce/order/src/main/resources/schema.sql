-- создаём таблицу orders
CREATE TABLE IF NOT EXISTS orders (
    order_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    shopping_cart_id UUID,
    username VARCHAR,
    payment_id UUID,
    delivery_id UUID,
    order_state VARCHAR,
    delivery_weight DECIMAL(10, 2),
    delivery_volume DECIMAL(10, 2),
    is_fragile boolean NOT NULL,
    total_price DECIMAL(10, 2),
    delivery_price DECIMAL(10, 2),
    product_price DECIMAL(10, 2),
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
    to_flat VARCHAR
    );

CREATE TABLE IF NOT EXISTS order_products (
    order_id UUID NOT NULL,
    product_id UUID,
    quantity INTEGER,
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);