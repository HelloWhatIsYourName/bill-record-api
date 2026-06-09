CREATE TABLE transactions (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES users(id),
    account_id uuid NOT NULL REFERENCES accounts(id),
    transfer_account_id uuid REFERENCES accounts(id),
    category_id uuid REFERENCES categories(id),
    type varchar(20) NOT NULL,
    amount numeric(19,2) NOT NULL,
    currency char(3) NOT NULL DEFAULT 'CNY',
    transaction_time timestamptz NOT NULL,
    note varchar(500),
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    CONSTRAINT ck_transactions_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_transactions_shape CHECK (
        (type = 'TRANSFER' AND transfer_account_id IS NOT NULL AND category_id IS NULL)
        OR
        (type IN ('INCOME', 'EXPENSE') AND transfer_account_id IS NULL AND category_id IS NOT NULL)
    )
);

CREATE INDEX ix_transactions_user_time ON transactions (user_id, transaction_time DESC);
CREATE INDEX ix_transactions_user_account ON transactions (user_id, account_id);
CREATE INDEX ix_transactions_user_category ON transactions (user_id, category_id);
