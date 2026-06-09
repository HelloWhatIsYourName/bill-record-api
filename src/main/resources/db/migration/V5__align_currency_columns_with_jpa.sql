ALTER TABLE users
    ALTER COLUMN default_currency TYPE varchar(3);

ALTER TABLE accounts
    ALTER COLUMN currency TYPE varchar(3);

ALTER TABLE transactions
    ALTER COLUMN currency TYPE varchar(3);

ALTER TABLE budgets
    ALTER COLUMN currency TYPE varchar(3);
