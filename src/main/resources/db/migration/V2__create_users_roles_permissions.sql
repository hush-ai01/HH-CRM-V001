CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                       company_id UUID NOT NULL,

                       email VARCHAR(255) NOT NULL,

                       password_hash VARCHAR(255) NOT NULL,

                       first_name VARCHAR(100) NOT NULL,

                       last_name VARCHAR(100) NOT NULL,

                       active BOOLEAN NOT NULL DEFAULT TRUE,

                       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_users_company
                           FOREIGN KEY (company_id)
                               REFERENCES companies(id),

                       CONSTRAINT uq_users_company_email
                           UNIQUE (company_id, email)
);


CREATE TABLE roles (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                       company_id UUID NOT NULL,

                       name VARCHAR(100) NOT NULL,

                       description VARCHAR(255),

                       active BOOLEAN NOT NULL DEFAULT TRUE,

                       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_roles_company
                           FOREIGN KEY (company_id)
                               REFERENCES companies(id),

                       CONSTRAINT uq_roles_company_name
                           UNIQUE (company_id, name)
);


CREATE TABLE permissions (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                             code VARCHAR(100) NOT NULL,

                             description VARCHAR(255),

                             CONSTRAINT uq_permissions_code
                                 UNIQUE (code)
);


CREATE TABLE user_roles (
                            user_id UUID NOT NULL,

                            role_id UUID NOT NULL,

                            PRIMARY KEY (user_id, role_id),

                            CONSTRAINT fk_user_roles_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users(id),

                            CONSTRAINT fk_user_roles_role
                                FOREIGN KEY (role_id)
                                    REFERENCES roles(id)
);


CREATE TABLE role_permissions (
                                  role_id UUID NOT NULL,

                                  permission_id UUID NOT NULL,

                                  PRIMARY KEY (role_id, permission_id),

                                  CONSTRAINT fk_role_permissions_role
                                      FOREIGN KEY (role_id)
                                          REFERENCES roles(id),

                                  CONSTRAINT fk_role_permissions_permission
                                      FOREIGN KEY (permission_id)
                                          REFERENCES permissions(id)
);


CREATE INDEX idx_users_company_id
    ON users(company_id);

CREATE INDEX idx_roles_company_id
    ON roles(company_id);

CREATE INDEX idx_user_roles_role_id
    ON user_roles(role_id);

CREATE INDEX idx_role_permissions_permission_id
    ON role_permissions(permission_id);