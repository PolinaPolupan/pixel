CREATE TABLE graphs (
    id TEXT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    schedule TEXT,
    nodes JSON,
    version BIGINT NOT NULL DEFAULT 0
);