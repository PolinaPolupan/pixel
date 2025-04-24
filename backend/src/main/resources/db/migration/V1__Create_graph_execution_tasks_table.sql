CREATE TABLE graph_execution_tasks (
    id BIGSERIAL PRIMARY KEY,
    scene_id VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    total_nodes INTEGER,
    processed_nodes INTEGER,
    error_message TEXT,
    version BIGINT NOT NULL DEFAULT 0

);

CREATE INDEX idx_graph_execution_tasks_status ON graph_execution_tasks(status);

COMMENT ON TABLE graph_execution_tasks IS 'Stores the state of asynchronous graph execution tasks';
COMMENT ON COLUMN graph_execution_tasks.scene_id IS 'Unique identifier for the scene being processed';
COMMENT ON COLUMN graph_execution_tasks.status IS 'Current status of the task (PENDING, RUNNING, COMPLETED, FAILED)';
COMMENT ON COLUMN graph_execution_tasks.version IS 'Optimistic locking version for concurrent updates';