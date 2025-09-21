CREATE TABLE graphs (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP,
    last_accessed TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);