CREATE TABLE nodes (
   id BIGSERIAL PRIMARY KEY,
   type TEXT NOT NULL,
   version BIGINT NOT NULL DEFAULT 0,
   inputs JSON,
   outputs JSON,
   display JSON,
   created_at TIMESTAMP NOT NULL DEFAULT now(),
   active BOOLEAN NOT NULL DEFAULT true,
   UNIQUE (type, version)
);
