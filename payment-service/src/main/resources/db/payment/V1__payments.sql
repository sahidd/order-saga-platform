CREATE TABLE payments (
    order_id uuid PRIMARY KEY,
    status varchar(20) NOT NULL,
    amount numeric(19, 2) NOT NULL CHECK (amount >= 0),
    method varchar(40) NOT NULL,
    reason varchar(200),
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
