CREATE TABLE categories (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    image_url   VARCHAR(500),
    active      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);
