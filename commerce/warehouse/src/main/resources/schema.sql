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


