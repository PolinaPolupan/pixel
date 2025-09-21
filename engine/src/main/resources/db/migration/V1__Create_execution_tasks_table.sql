CREATE TABLE execution_tasks (
    id BIGSERIAL PRIMARY KEY,
    graph_id BIGSERIAL NOT NULL,
    status VARCHAR(20) NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    total_nodes INTEGER,
    processed_nodes INTEGER,
    error_message TEXT,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_execution_tasks_status ON execution_tasks(status);

COMMENT ON TABLE execution_tasks IS 'Stores the state of asynchronous executionGraph execution tasks';
COMMENT ON COLUMN execution_tasks.graph_id IS 'Unique identifier for the graphModel being processed';
COMMENT ON COLUMN execution_tasks.status IS 'Current status of the task (PENDING, RUNNING, COMPLETED, FAILED)';
COMMENT ON COLUMN execution_tasks.version IS 'Optimistic locking version for concurrent updates';