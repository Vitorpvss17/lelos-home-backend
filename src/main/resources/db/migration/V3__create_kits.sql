CREATE TABLE kits (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    type        VARCHAR(10)  NOT NULL CHECK (type IN ('SALE','RENT','BOTH')),
    sale_price  NUMERIC(10,2),
    rent_price  NUMERIC(10,2),
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE kit_images (
    kit_id    UUID         NOT NULL REFERENCES kits(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL
);

CREATE TABLE kit_items (
    id         UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    kit_id     UUID    NOT NULL REFERENCES kits(id) ON DELETE CASCADE,
    product_id UUID    NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    quantity   INTEGER NOT NULL CHECK (quantity > 0)
);

CREATE INDEX idx_kit_items_kit     ON kit_items(kit_id);
CREATE INDEX idx_kit_items_product ON kit_items(product_id);
