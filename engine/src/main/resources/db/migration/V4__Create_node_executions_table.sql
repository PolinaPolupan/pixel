CREATE TABLE node_executions (
    id BIGSERIAL PRIMARY KEY,
    graph_execution_id BIGINT NOT NULL,
    node_id BIGINT NOT NULL,
    node_type VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    inputs JSON,
    outputs JSON,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    error_message TEXT,

    CONSTRAINT fk_node_executions_graph_execution
        FOREIGN KEY (graph_execution_id)
            REFERENCES graph_executions(id)
            ON DELETE CASCADE
);

CREATE INDEX idx_node_executions_graph_execution_id ON node_executions(graph_execution_id);
CREATE INDEX idx_node_executions_status ON node_executions(status);