INSERT INTO permissions (code, description)
VALUES
    ('INVENTORY_CREATE', 'Create inventory'),
    ('INVENTORY_READ', 'View inventory'),
    ('INVENTORY_UPDATE', 'Update inventory'),
    ('INVENTORY_DELETE', 'Delete inventory')
    ON CONFLICT (code) DO NOTHING;