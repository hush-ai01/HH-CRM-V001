INSERT INTO permissions (code, description)
VALUES
    ('TRIP_CREATE', 'Create trips'),
    ('TRIP_READ', 'Read trips'),
    ('TRIP_UPDATE', 'Update trips'),
    ('TRIP_DELETE', 'Delete trips')
    ON CONFLICT (code) DO NOTHING;