CREATE TABLE drivers (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                         company_id UUID NOT NULL,

                         first_name VARCHAR(100) NOT NULL,
                         last_name VARCHAR(100) NOT NULL,

                         identification_type VARCHAR(30) NOT NULL,
                         identification_number VARCHAR(50) NOT NULL,

                         phone VARCHAR(30) NOT NULL,

                         created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                         updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                         CONSTRAINT fk_drivers_company
                             FOREIGN KEY (company_id)
                                 REFERENCES companies(id),

                         CONSTRAINT uq_drivers_company_identification
                             UNIQUE (company_id, identification_type, identification_number)
);

CREATE INDEX idx_drivers_company_id
    ON drivers(company_id);

CREATE INDEX idx_drivers_company_name
    ON drivers(company_id, last_name, first_name);

CREATE INDEX idx_drivers_company_identification
    ON drivers(company_id, identification_type, identification_number);