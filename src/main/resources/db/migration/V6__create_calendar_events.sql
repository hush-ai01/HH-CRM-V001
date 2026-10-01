CREATE TABLE calendar_events (
                                 id UUID PRIMARY KEY,
                                 company_id UUID NOT NULL,
                                 owner_user_id UUID NOT NULL,

                                 title VARCHAR(255) NOT NULL,
                                 type VARCHAR(30) NOT NULL,
                                 scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                 notes TEXT,

                                 completed BOOLEAN NOT NULL DEFAULT FALSE,

                                 linked_entity_type VARCHAR(50),
                                 linked_entity_id UUID,

                                 created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                 updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                 CONSTRAINT fk_calendar_events_company
                                     FOREIGN KEY (company_id)
                                         REFERENCES companies(id),

                                 CONSTRAINT fk_calendar_events_owner
                                     FOREIGN KEY (owner_user_id)
                                         REFERENCES users(id)
);

CREATE INDEX idx_calendar_events_company_id
    ON calendar_events(company_id);

CREATE INDEX idx_calendar_events_owner_user_id
    ON calendar_events(owner_user_id);

CREATE INDEX idx_calendar_events_scheduled_at
    ON calendar_events(company_id, scheduled_at);

CREATE INDEX idx_calendar_events_linked_entity
    ON calendar_events(linked_entity_type, linked_entity_id);