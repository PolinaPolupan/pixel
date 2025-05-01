CREATE TABLE file_metadata (
    id UUID NOT NULL DEFAULT uuid_generate_v4(),
    name VARCHAR(255),
    relative_storage_path VARCHAR(1024),
    storage_path VARCHAR(1024),
    check_sum VARCHAR(64)
);