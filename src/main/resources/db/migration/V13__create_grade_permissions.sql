INSERT INTO permissions (code, description)
VALUES
    ('GRADE_CREATE', 'Create grades'),
    ('GRADE_READ', 'View grades'),
    ('GRADE_UPDATE', 'Update grades'),
    ('GRADE_DELETE', 'Delete grades')
    ON CONFLICT (code) DO NOTHING;