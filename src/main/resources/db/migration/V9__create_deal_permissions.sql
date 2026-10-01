INSERT INTO permissions (code, description)
VALUES
    (
        'DEAL_DELETE',
        'Delete deals and transactions'
    )
    ON CONFLICT (code) DO NOTHING;