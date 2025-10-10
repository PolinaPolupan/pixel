CREATE TABLE graph_executions (
    id BIGSERIAL PRIMARY KEY,
    graph_id VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    total_nodes INTEGER,
    processed_nodes INTEGER,
    error_message TEXT,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_graph_executions_status ON graph_executions(status);
