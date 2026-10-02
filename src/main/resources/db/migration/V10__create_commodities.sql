CREATE TABLE commodities (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                             company_id UUID NOT NULL,

                             name VARCHAR(100) NOT NULL,

                             code VARCHAR(50) NOT NULL,

                             description TEXT,

                             active BOOLEAN NOT NULL DEFAULT TRUE,

                             created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                             updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                             CONSTRAINT fk_commodities_company
                                 FOREIGN KEY (company_id)
                                     REFERENCES companies(id),

                             CONSTRAINT uq_commodities_company_code
                                 UNIQUE (company_id, code)
);

CREATE INDEX idx_commodities_company_id
    ON commodities(company_id);

CREATE INDEX idx_commodities_company_active
    ON commodities(company_id, active);

CREATE INDEX idx_commodities_company_name
    ON commodities(company_id, name);