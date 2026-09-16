CREATE TABLE clients (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                         company_id UUID NOT NULL,
                         name VARCHAR(255) NOT NULL,

                         type VARCHAR(50) NOT NULL,
                         area VARCHAR(255),

                         monthly_quantity NUMERIC(19, 3),
                         weekly_quantity NUMERIC(19, 3),

                         payment_terms VARCHAR(255),
                         delivery_terms VARCHAR(50),
                         warehouse VARCHAR(255),

                         contact_name VARCHAR(255),
                         contact_phone VARCHAR(100),
                         contact_email VARCHAR(255),

                         account_status VARCHAR(50) NOT NULL,

                         next_action VARCHAR(500),
                         next_action_at TIMESTAMPTZ,

                         owner_user_id UUID,

                         visibility VARCHAR(50) NOT NULL,

                         source_lead_id UUID,

                         originator_user_id UUID,
                         created_by_user_id UUID NOT NULL,

                         created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT fk_clients_company
                             FOREIGN KEY (company_id)
                                 REFERENCES companies(id),

                         CONSTRAINT fk_clients_owner
                             FOREIGN KEY (owner_user_id)
                                 REFERENCES users(id),

                         CONSTRAINT fk_clients_created_by
                             FOREIGN KEY (created_by_user_id)
                                 REFERENCES users(id)
);