CREATE TABLE accounts (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES users(id),
    name varchar(80) NOT NULL,
    type varchar(30) NOT NULL,
    currency char(3) NOT NULL DEFAULT 'CNY',
    opening_balance numeric(19,2) NOT NULL,
    current_balance numeric(19,2) NOT NULL,
    archived boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);

CREATE UNIQUE INDEX ux_accounts_user_name_lower ON accounts (user_id, lower(name));
CREATE INDEX ix_accounts_user_active ON accounts (user_id, archived);

CREATE TABLE categories (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES users(id),
    name varchar(80) NOT NULL,
    type varchar(20) NOT NULL,
    color varchar(20),
    icon varchar(40),
    archived boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);

CREATE UNIQUE INDEX ux_categories_user_type_name_lower ON categories (user_id, type, lower(name));
CREATE INDEX ix_categories_user_type_active ON categories (user_id, type, archived);
