CREATE TABLE trips (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                       company_id UUID NOT NULL,

                       dispatch_ref VARCHAR(100) NOT NULL,

                       dispatch_date TIMESTAMP WITH TIME ZONE NOT NULL,

                       scheduled_pickup TIMESTAMP WITH TIME ZONE NOT NULL,

                       eta TIMESTAMP WITH TIME ZONE,

                       origin VARCHAR(255) NOT NULL,

                       destination VARCHAR(255) NOT NULL,

                       current_location VARCHAR(255),

                       tracking_url VARCHAR(1000),

                       dispatcher_notes TEXT,

                       last_location_at TIMESTAMP WITH TIME ZONE,

                       driver_id UUID NOT NULL,

                       truck_id UUID NOT NULL,

                       trailer_id UUID NOT NULL,

                       grade_id UUID,

                       gross_weight NUMERIC(19,4),

                       tare_weight NUMERIC(19,4),

                       net_weight NUMERIC(19,4),

                       moisture_pct NUMERIC(7,4),

                       status VARCHAR(40) NOT NULL DEFAULT 'ASSIGNED',

                       created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                       updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                       CONSTRAINT fk_trips_company
                           FOREIGN KEY (company_id)
                               REFERENCES companies(id),

                       CONSTRAINT fk_trips_driver
                           FOREIGN KEY (driver_id)
                               REFERENCES drivers(id),

                       CONSTRAINT fk_trips_truck
                           FOREIGN KEY (truck_id)
                               REFERENCES trucks(id),

                       CONSTRAINT fk_trips_trailer
                           FOREIGN KEY (trailer_id)
                               REFERENCES trailers(id),

                       CONSTRAINT fk_trips_grade
                           FOREIGN KEY (grade_id)
                               REFERENCES grades(id),

                       CONSTRAINT uq_trips_company_dispatch_ref
                           UNIQUE (company_id, dispatch_ref),

                       CONSTRAINT chk_trips_gross_weight_positive
                           CHECK (gross_weight IS NULL OR gross_weight >= 0),

                       CONSTRAINT chk_trips_tare_weight_positive
                           CHECK (tare_weight IS NULL OR tare_weight >= 0),

                       CONSTRAINT chk_trips_net_weight_positive
                           CHECK (net_weight IS NULL OR net_weight >= 0),

                       CONSTRAINT chk_trips_moisture_pct
                           CHECK (
                                   moisture_pct IS NULL
                                   OR (moisture_pct >= 0 AND moisture_pct <= 100)
                               )
);

CREATE INDEX idx_trips_company_id
    ON trips(company_id);

CREATE INDEX idx_trips_company_status
    ON trips(company_id, status);

CREATE INDEX idx_trips_company_dispatch_date
    ON trips(company_id, dispatch_date);

CREATE INDEX idx_trips_company_scheduled_pickup
    ON trips(company_id, scheduled_pickup);

CREATE INDEX idx_trips_driver_id
    ON trips(driver_id);

CREATE INDEX idx_trips_truck_id
    ON trips(truck_id);

CREATE INDEX idx_trips_trailer_id
    ON trips(trailer_id);

CREATE INDEX idx_trips_grade_id
    ON trips(grade_id);

CREATE INDEX idx_trips_company_destination
    ON trips(company_id, destination);