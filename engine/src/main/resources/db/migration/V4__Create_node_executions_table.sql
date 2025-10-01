CREATE TABLE node_executions (
    id BIGSERIAL PRIMARY KEY,
    graph_execution_id BIGSERIAL NOT NULL,
    status VARCHAR(20) NOT NULL,
    inputs JSON,
    outputs JSON,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    error_message TEXT
);