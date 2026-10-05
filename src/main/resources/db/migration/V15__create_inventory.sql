CREATE TABLE inventory (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                           company_id UUID NOT NULL,

                           commodity_id UUID NOT NULL,

                           grade_id UUID NOT NULL,

                           supplier_client_id UUID,

                           quantity NUMERIC(19,4) NOT NULL,

                           unit VARCHAR(20) NOT NULL,

                           location VARCHAR(255) NOT NULL,

                           wash_plant VARCHAR(255),

                           owner VARCHAR(255),

                           status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',

                           portal_submitted BOOLEAN NOT NULL DEFAULT FALSE,

                           notes TEXT,

                           created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                           updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                           CONSTRAINT fk_inventory_company
                               FOREIGN KEY (company_id)
                                   REFERENCES companies(id),

                           CONSTRAINT fk_inventory_commodity
                               FOREIGN KEY (commodity_id)
                                   REFERENCES commodities(id),

                           CONSTRAINT fk_inventory_grade
                               FOREIGN KEY (grade_id)
                                   REFERENCES grades(id),

                           CONSTRAINT fk_inventory_supplier_client
                               FOREIGN KEY (supplier_client_id)
                                   REFERENCES clients(id)
);

CREATE INDEX idx_inventory_company_id
    ON inventory(company_id);

CREATE INDEX idx_inventory_commodity_id
    ON inventory(commodity_id);

CREATE INDEX idx_inventory_grade_id
    ON inventory(grade_id);

CREATE INDEX idx_inventory_supplier_client_id
    ON inventory(supplier_client_id);

CREATE INDEX idx_inventory_company_status
    ON inventory(company_id, status);

CREATE INDEX idx_inventory_company_commodity
    ON inventory(company_id, commodity_id);

CREATE INDEX idx_inventory_company_grade
    ON inventory(company_id, grade_id);
