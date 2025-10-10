CREATE TABLE connection (
    id SERIAL PRIMARY KEY,
    conn_id VARCHAR(255) NOT NULL UNIQUE,
    conn_type VARCHAR(255) NOT NULL,
    host VARCHAR(255),
    schema VARCHAR(255),
    login VARCHAR(255),
    password VARCHAR(255),
    port INT,
    extra TEXT
);