CREATE TABLE admin_users (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email      VARCHAR(200) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    name       VARCHAR(100),
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);
