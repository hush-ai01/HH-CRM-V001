INSERT INTO permissions (code, description)
VALUES
    ('COMPANY_READ', 'View company information'),
    ('COMPANY_UPDATE', 'Update company information'),

    ('USER_READ', 'View users'),
    ('USER_CREATE', 'Create users'),
    ('USER_UPDATE', 'Update users'),
    ('USER_DEACTIVATE', 'Deactivate users'),

    ('ROLE_READ', 'View roles'),
    ('ROLE_CREATE', 'Create roles'),
    ('ROLE_UPDATE', 'Update roles'),
    ('ROLE_DEACTIVATE', 'Deactivate roles'),

    ('PERMISSION_READ', 'View available permissions'),

    ('CLIENT_READ', 'View clients'),
    ('CLIENT_CREATE', 'Create clients'),
    ('CLIENT_UPDATE', 'Update clients'),
    ('CLIENT_DELETE', 'Delete clients'),

    ('BUYER_LEAD_READ', 'View buyer pipeline leads'),
    ('BUYER_LEAD_CREATE', 'Create buyer pipeline leads'),
    ('BUYER_LEAD_UPDATE', 'Update buyer pipeline leads'),
    ('BUYER_LEAD_DELETE', 'Delete buyer pipeline leads'),
    ('BUYER_LEAD_CONVERT', 'Convert buyer lead to client'),

    ('DEAL_READ', 'View deals and transactions'),
    ('DEAL_CREATE', 'Create deals and transactions'),
    ('DEAL_UPDATE', 'Update deals and transactions'),

    ('TRUCK_READ', 'View trucks'),
    ('TRUCK_CREATE', 'Create trucks'),
    ('TRUCK_UPDATE', 'Update trucks'),

    ('TRAILER_READ', 'View trailers'),
    ('TRAILER_CREATE', 'Create trailers'),
    ('TRAILER_UPDATE', 'Update trailers'),

    ('DRIVER_READ', 'View drivers'),
    ('DRIVER_CREATE', 'Create drivers'),
    ('DRIVER_UPDATE', 'Update drivers'),

    ('TRIP_READ', 'View trips and dispatches'),
    ('TRIP_CREATE', 'Create trips and dispatches'),
    ('TRIP_UPDATE', 'Update trips and dispatches'),

    ('WEIGHBRIDGE_READ', 'View weighbridge records'),
    ('WEIGHBRIDGE_CREATE', 'Create weighbridge records'),
    ('WEIGHBRIDGE_UPDATE', 'Update weighbridge records'),

    ('ASSAY_READ', 'View assay results'),
    ('ASSAY_CREATE', 'Create assay results'),
    ('ASSAY_UPDATE', 'Update assay results'),

    ('INVOICE_READ', 'View invoices'),
    ('INVOICE_CREATE', 'Create invoices'),
    ('INVOICE_UPDATE', 'Update invoices'),

    ('DOCUMENT_READ', 'View documents'),
    ('DOCUMENT_CREATE', 'Upload documents'),
    ('DOCUMENT_UPDATE', 'Update document metadata'),
    ('DOCUMENT_DELETE', 'Delete documents'),

    ('CALENDAR_READ', 'View calendar events'),
    ('CALENDAR_CREATE', 'Create calendar events'),
    ('CALENDAR_UPDATE', 'Update calendar events'),
    ('CALENDAR_DELETE', 'Delete calendar events'),

    ('REPORT_READ', 'View reports'),

    ('AUDIT_READ', 'View audit logs'),

    ('SETTINGS_READ', 'View company settings'),
    ('SETTINGS_UPDATE', 'Update company settings')

    ON CONFLICT (code) DO NOTHING;