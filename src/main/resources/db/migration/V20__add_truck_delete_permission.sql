INSERT INTO permissions (code, description)
VALUES (
    'TRUCK_DELETE',
    'Delete trucks'
)
ON CONFLICT (code) DO NOTHING;
