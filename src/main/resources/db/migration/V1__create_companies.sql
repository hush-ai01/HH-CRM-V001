CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE companies (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                           name VARCHAR(255) NOT NULL,

                           code VARCHAR(100) NOT NULL UNIQUE,

                           active BOOLEAN NOT NULL DEFAULT TRUE,

                           created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);