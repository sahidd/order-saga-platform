CREATE TABLE products (
    sku varchar(40) PRIMARY KEY,
    name varchar(80) NOT NULL,
    price numeric(19, 2) NOT NULL CHECK (price >= 0),
    available integer NOT NULL CHECK (available >= 0),
    version bigint NOT NULL
);

CREATE TABLE reservations (
    order_id uuid PRIMARY KEY,
    sku varchar(40) NOT NULL,
    quantity integer NOT NULL CHECK (quantity > 0),
    released boolean NOT NULL,
    created_at timestamptz NOT NULL
);

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
