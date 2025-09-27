CREATE TABLE node_executions (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(20) NOT NULL,
    input_values JSON,
    output_values JSON,
    started_at TIMESTAMP,
    finished_at TIMESTAMP
);