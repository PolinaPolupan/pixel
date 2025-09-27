CREATE TABLE nodes (
   id BIGSERIAL PRIMARY KEY,
   node_type TEXT NOT NULL,
   version BIGINT NOT NULL DEFAULT 0,
   inputs_config JSON,
   outputs_config JSON,
   display_config JSON,
   created_at TIMESTAMP NOT NULL DEFAULT now(),
   active BOOLEAN NOT NULL DEFAULT true,
   UNIQUE (node_type, version)
);
