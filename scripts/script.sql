CREATE TABLE currencies(
    id int PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
    code varchar(128) NOT NULL UNIQUE,
    fullname varchar(128) NOT NULL,
    sign varchar(128) NOT NULL
);

CREATE TABLE exchange_rates(
    id int PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
    base_currency_id int REFERENCES currencies(id),
    target_currency_id int REFERENCES currencies(id),
    rate decimal(6) NOT NULL,
    CONSTRAINT unique_base_id_target_id UNIQUE (base_currency_id, target_currency_id)
)