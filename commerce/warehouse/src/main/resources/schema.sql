-- создаём таблицу products
CREATE TABLE IF NOT EXISTS products (
    product_id UUID PRIMARY KEY,
    is_fragile boolean NOT NULL,
    width DECIMAL(10, 2) NOT NULL,
    height DECIMAL(11, 2) NOT NULL,
    depth DECIMAL(10, 2) NOT NULL,
    weight DECIMAL(10, 2) NOT NULL,
    quantity INTEGER
);

CREATE TABLE IF NOT EXISTS order_bookings (
    order_booking_id UUID PRIMARY KEY,
    order_id UUID,
    delivery_id UUID
);

CREATE TABLE IF NOT EXISTS order_booking_products (
    order_booking_id UUID NOT NULL,
    product_id UUID,
    quantity INTEGER,
    FOREIGN KEY (order_booking_id) REFERENCES order_bookings(order_booking_id)
);


