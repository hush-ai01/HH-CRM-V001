CREATE TABLE grades (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        company_id UUID NOT NULL,
                        commodity_id UUID NOT NULL,
                        name VARCHAR(100) NOT NULL,
                        code VARCHAR(50) NOT NULL,
                        description TEXT,
                        active BOOLEAN NOT NULL DEFAULT TRUE,
                        created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                        updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                        CONSTRAINT fk_grades_company
                            FOREIGN KEY (company_id)
                                REFERENCES companies(id),

                        CONSTRAINT fk_grades_commodity
                            FOREIGN KEY (commodity_id)
                                REFERENCES commodities(id),

                        CONSTRAINT uq_grades_company_commodity_code
                            UNIQUE (company_id, commodity_id, code)
);

CREATE INDEX idx_grades_company_id
    ON grades(company_id);

CREATE INDEX idx_grades_commodity_id
    ON grades(commodity_id);

CREATE INDEX idx_grades_company_commodity
    ON grades(company_id, commodity_id);

CREATE INDEX idx_grades_company_active
    ON grades(company_id, active);

CREATE INDEX idx_grades_company_commodity_active
    ON grades(company_id, commodity_id, active);

CREATE INDEX idx_grades_company_name
    ON grades(company_id, name);