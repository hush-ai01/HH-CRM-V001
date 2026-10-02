INSERT INTO permissions (code, description)
VALUES
    ('COMMODITY_CREATE', 'Create commodities'),
    ('COMMODITY_READ', 'View commodities'),
    ('COMMODITY_UPDATE', 'Update commodities'),
    ('COMMODITY_DELETE', 'Delete commodities')
    ON CONFLICT (code) DO NOTHING;