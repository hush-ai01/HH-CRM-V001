INSERT INTO permissions (code, description)
VALUES (
           'DRIVER_DELETE',
           'Delete drivers'
       )
    ON CONFLICT (code) DO NOTHING;