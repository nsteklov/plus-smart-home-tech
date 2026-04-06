-- создаём таблицу products
CREATE TABLE IF NOT EXISTS shopping_cart (
    shopping_cart_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    username VARCHAR
);

CREATE TABLE IF NOT EXISTS shopping_cart_products (
    shopping_cart_id UUID NOT NULL,
    product_id UUID,
    quantity INTEGER,
    FOREIGN KEY (shopping_cart_id) REFERENCES shopping_cart(shopping_cart_id)
);