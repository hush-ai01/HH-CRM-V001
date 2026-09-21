CREATE TABLE activities (
                            id UUID PRIMARY KEY,
                            company_id UUID NOT NULL,
                            client_id UUID NOT NULL,
                            user_id UUID NOT NULL,
                            type VARCHAR(30) NOT NULL,
                            outcome TEXT,
                            notes TEXT,
                            next_action TEXT,
                            next_action_at TIMESTAMP WITH TIME ZONE,
                            created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                            CONSTRAINT fk_activities_company
                                FOREIGN KEY (company_id)
                                    REFERENCES companies(id),

                            CONSTRAINT fk_activities_client
                                FOREIGN KEY (client_id)
                                    REFERENCES clients(id),

                            CONSTRAINT fk_activities_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users(id)
);

CREATE INDEX idx_activities_company_id
    ON activities(company_id);

CREATE INDEX idx_activities_client_id
    ON activities(client_id);

CREATE INDEX idx_activities_user_id
    ON activities(user_id);

CREATE INDEX idx_activities_client_created_at
    ON activities(client_id, created_at DESC);