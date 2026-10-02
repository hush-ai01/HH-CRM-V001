ALTER TABLE deals
    ADD COLUMN commodity_id UUID NOT NULL,
    ADD COLUMN grade_id UUID NOT NULL;

ALTER TABLE deals
    ADD CONSTRAINT fk_deals_commodity
        FOREIGN KEY (commodity_id)
            REFERENCES commodities(id);

ALTER TABLE deals
    ADD CONSTRAINT fk_deals_grade
        FOREIGN KEY (grade_id)
            REFERENCES grades(id);

CREATE INDEX idx_deals_commodity_id
    ON deals(commodity_id);

CREATE INDEX idx_deals_grade_id
    ON deals(grade_id);

CREATE INDEX idx_deals_company_commodity
    ON deals(company_id, commodity_id);

CREATE INDEX idx_deals_company_grade
    ON deals(company_id, grade_id);

ALTER TABLE deals
DROP COLUMN commodity;

ALTER TABLE deals
DROP COLUMN grade;