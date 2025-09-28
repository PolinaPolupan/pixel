CREATE TABLE node_executions (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(20) NOT NULL,
    inputs JSON,
    outputs JSON,
    started_at TIMESTAMP,
    finished_at TIMESTAMP
);