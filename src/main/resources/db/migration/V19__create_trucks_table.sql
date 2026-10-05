CREATE TABLE trucks (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                        company_id UUID NOT NULL,

                        registration_number VARCHAR(50) NOT NULL,

                        make VARCHAR(100) NOT NULL,

                        model VARCHAR(100) NOT NULL,

                        capacity NUMERIC(19,4) NOT NULL,

                        status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',

                        assigned_company VARCHAR(150),

                        assigned_owner VARCHAR(150),

                        created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                        updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                        CONSTRAINT fk_trucks_company
                            FOREIGN KEY (company_id)
                                REFERENCES companies(id),

                        CONSTRAINT uq_trucks_company_registration
                            UNIQUE (company_id, registration_number),

                        CONSTRAINT chk_trucks_capacity_positive
                            CHECK (capacity > 0)
);

CREATE INDEX idx_trucks_company_id
    ON trucks(company_id);

CREATE INDEX idx_trucks_company_status
    ON trucks(company_id, status);

CREATE INDEX idx_trucks_company_registration
    ON trucks(company_id, registration_number);