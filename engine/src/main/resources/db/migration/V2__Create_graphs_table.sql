CREATE TABLE graphs (
    id SERIAL PRIMARY KEY,
    graph_id VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    schedule VARCHAR(255),
    nodes JSON,
    version BIGINT NOT NULL DEFAULT 0
);