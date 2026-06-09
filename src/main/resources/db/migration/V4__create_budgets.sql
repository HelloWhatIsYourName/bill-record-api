CREATE TABLE budgets (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES users(id),
    category_id uuid NOT NULL REFERENCES categories(id),
    month date NOT NULL,
    amount numeric(19,2) NOT NULL,
    currency char(3) NOT NULL DEFAULT 'CNY',
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    CONSTRAINT ck_budgets_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_budgets_month_first_day CHECK (extract(day from month) = 1)
);

CREATE UNIQUE INDEX ux_budgets_user_category_month ON budgets (user_id, category_id, month);
