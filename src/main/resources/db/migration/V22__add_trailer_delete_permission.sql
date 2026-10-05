INSERT INTO permissions (code, description)
VALUES (
    'TRAILER_DELETE',
    'Delete trailers'
)
ON CONFLICT (code) DO NOTHING;
