CREATE TABLE graph_executions (
    id BIGSERIAL PRIMARY KEY,
    graph_id VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    total_nodes INTEGER,
    processed_nodes INTEGER,
    error_message TEXT,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_graph_executions_graph
      FOREIGN KEY (graph_id)
          REFERENCES graphs(graph_id)
          ON DELETE CASCADE
);

CREATE INDEX idx_graph_executions_status ON graph_executions(status);
CREATE INDEX idx_graph_executions_graph_id ON graph_executions(graph_id);
CREATE INDEX idx_graph_executions_start_time ON graph_executions(start_time DESC);