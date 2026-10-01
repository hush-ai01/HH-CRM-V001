CREATE TABLE deals (
                       id UUID PRIMARY KEY,

                       company_id UUID NOT NULL,
                       client_id UUID NOT NULL,
                       owner_user_id UUID NOT NULL,

                       deal_number VARCHAR(50) NOT NULL,

                       type VARCHAR(20) NOT NULL,
                       status VARCHAR(30) NOT NULL,

                       commodity VARCHAR(100) NOT NULL,
                       grade VARCHAR(100),

                       quantity NUMERIC(19,4) NOT NULL,
                       unit VARCHAR(20) NOT NULL,

                       unit_price NUMERIC(19,4) NOT NULL,
                       currency VARCHAR(3) NOT NULL,

                       total_value NUMERIC(19,4) NOT NULL,

                       expected_close_date DATE,

                       notes TEXT,

                       created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                       updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                       CONSTRAINT fk_deals_company
                           FOREIGN KEY (company_id)
                               REFERENCES companies(id),

                       CONSTRAINT fk_deals_client
                           FOREIGN KEY (client_id)
                               REFERENCES clients(id),

                       CONSTRAINT fk_deals_owner
                           FOREIGN KEY (owner_user_id)
                               REFERENCES users(id),

                       CONSTRAINT uk_deals_company_number
                           UNIQUE (company_id, deal_number)
);

CREATE INDEX idx_deals_company_id
    ON deals(company_id);

CREATE INDEX idx_deals_client_id
    ON deals(client_id);

CREATE INDEX idx_deals_owner_user_id
    ON deals(owner_user_id);

CREATE INDEX idx_deals_status
    ON deals(status);

CREATE INDEX idx_deals_company_status
    ON deals(company_id, status);