CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(120) NOT NULL,
    display_name VARCHAR(80) NOT NULL,
    default_currency CHAR(3) NOT NULL DEFAULT 'CNY',
    role VARCHAR(30) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_users_email ON users (email);
