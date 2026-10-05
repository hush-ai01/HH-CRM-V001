CREATE TABLE trailers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    truck_id UUID NOT NULL,
    registration_number VARCHAR(50) NOT NULL,
    type VARCHAR(100) NOT NULL DEFAULT 'Side tipper',
    capacity NUMERIC(19,4) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_trailers_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id),

    CONSTRAINT fk_trailers_truck
        FOREIGN KEY (truck_id)
        REFERENCES trucks(id),

    CONSTRAINT uq_trailers_company_registration
        UNIQUE (company_id, registration_number),

    CONSTRAINT chk_trailers_capacity_positive
        CHECK (capacity > 0)
);

CREATE INDEX idx_trailers_company_id
    ON trailers(company_id);

CREATE INDEX idx_trailers_truck_id
    ON trailers(truck_id);

CREATE INDEX idx_trailers_company_truck
    ON trailers(company_id, truck_id);

CREATE INDEX idx_trailers_company_status
    ON trailers(company_id, status);

CREATE INDEX idx_trailers_company_registration
    ON trailers(company_id, registration_number);
