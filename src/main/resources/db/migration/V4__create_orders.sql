CREATE TABLE orders (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_name  VARCHAR(200),
    customer_phone VARCHAR(20),
    event_date     DATE,
    notes          TEXT,
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                               CHECK (status IN ('PENDING','SENT_TO_WHATSAPP','CANCELLED')),
    whatsapp_url   TEXT,
    created_at     TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE order_items (
    id         UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id   UUID          NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    item_type  VARCHAR(10)   NOT NULL CHECK (item_type IN ('PRODUCT','KIT')),
    product_id UUID          REFERENCES products(id) ON DELETE SET NULL,
    kit_id     UUID          REFERENCES kits(id) ON DELETE SET NULL,
    quantity   INTEGER       NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(10,2) NOT NULL,
    item_mode  VARCHAR(10)   NOT NULL CHECK (item_mode IN ('SALE','RENT'))
);

CREATE INDEX idx_orders_status     ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_order_items_order ON order_items(order_id);
