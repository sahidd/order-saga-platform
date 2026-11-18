CREATE TABLE orders (
    id uuid PRIMARY KEY,
    customer_name varchar(80) NOT NULL,
    sku varchar(40) NOT NULL,
    quantity integer NOT NULL CHECK (quantity > 0),
    amount numeric(19, 2) NOT NULL CHECK (amount >= 0),
    payment_method varchar(40) NOT NULL,
    status varchar(20) NOT NULL,
    created_at timestamptz NOT NULL
);

CREATE TABLE saga_events (
    event_id uuid PRIMARY KEY,
    order_id uuid NOT NULL,
    event_type varchar(40) NOT NULL,
    payload varchar(4000) NOT NULL,
    created_at timestamptz NOT NULL
);

CREATE INDEX saga_events_order_idx ON saga_events (order_id, created_at);

CREATE TABLE outbox_events (
    id uuid PRIMARY KEY,
    topic varchar(80) NOT NULL,
    aggregate_id varchar(80) NOT NULL,
    payload varchar(4000) NOT NULL,
    created_at timestamptz NOT NULL,
    published_at timestamptz
);

CREATE TABLE processed_events (
    event_id uuid PRIMARY KEY,
    processed_at timestamptz NOT NULL
);
