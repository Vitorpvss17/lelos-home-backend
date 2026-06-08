CREATE TABLE products (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    type        VARCHAR(10)  NOT NULL CHECK (type IN ('SALE','RENT','BOTH')),
    sale_price  NUMERIC(10,2),
    rent_price  NUMERIC(10,2),
    stock_qty   INTEGER      NOT NULL DEFAULT 0,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    category_id UUID         REFERENCES categories(id) ON DELETE SET NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE product_images (
    product_id UUID         NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url  VARCHAR(500) NOT NULL
);

CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_active   ON products(active);
CREATE INDEX idx_products_type     ON products(type);
