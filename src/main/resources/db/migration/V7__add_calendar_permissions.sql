INSERT INTO permissions (code, description)
VALUES
    ('CALENDAR_CREATE', 'Create calendar events'),
    ('CALENDAR_READ', 'View calendar events'),
    ('CALENDAR_UPDATE', 'Update calendar events')
    ON CONFLICT (code) DO NOTHING;