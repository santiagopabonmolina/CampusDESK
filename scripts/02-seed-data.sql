-- ==============================================================================
-- 02-seed-data.sql
-- CampusDESK - TechNova Solutions
-- DML: Datos iniciales de roles y cuentas administrativas/técnicas
-- ==============================================================================

-- 1. Catálogo oficial de Roles
-- Inserts standard system roles into role catalog table if absent
INSERT INTO role (name)
-- Supplies literal role enum names for authorization security
VALUES ('ADMIN'), ('TECHNICIAN'), ('USER')
-- Prevents duplicate insertion error if roles already exist
ON CONFLICT (name) DO NOTHING;

-- 2. Usuario Super Administrador
-- Email: admin@campusdesk.com | Password: SuperAdmin2026!
-- Inserts administrator user record with pre-computed hashed credentials
INSERT INTO users (first_name, last_name, email, password, active, register_date)
-- Supplies administrator profile details including registration timestamp
VALUES (
    -- Supplies given first name string for administrator user
    'Super',
    -- Supplies family surname string for administrator user
    'Admin',
    -- Supplies unique login email address for administrator user
    'admin@campusdesk.com',
    -- Stores salted BCrypt cryptographic hash for authentication
    '$2a$10$IUMAI5JsSqbY5YETYIapJOcTgRRn3qBrEstoLx6HB.aBTL/q7qfg2',
    -- Sets user account state active flag to true
    true,
    -- Sets initial registration timestamp to current database timestamp
    NOW()
-- Closes insert values expression for administrator user
)
-- Updates existing record on duplicate email conflict during seeding
ON CONFLICT (email) DO UPDATE 
-- Updates password hash column on existing record with excluded value
SET password = EXCLUDED.password;

-- 3. Usuario Técnico de Soporte
-- Email: tecnico@campusdesk.com | Password: Tech2026!
-- Inserts initial technical support specialist user account into users
INSERT INTO users (first_name, last_name, email, password, active, register_date)
-- Supplies technician profile details including registration timestamp
VALUES (
    -- Supplies given first name string for support technician
    'Carlos',
    -- Supplies family surname string for support technician
    'Soporte',
    -- Supplies unique institutional email for support technician
    'tecnico@campusdesk.com',
    -- Stores salted BCrypt cryptographic hash for authentication
    '$2a$10$4JB0UpDme52AL6F8uip7W.aWUT1Vm50T7I0H9UskRqPm3A1.kOXEm',
    -- Sets user account state active flag to true
    true,
    -- Sets initial registration timestamp to current database timestamp
    NOW()
-- Closes insert values expression for technician user
)
-- Updates existing record on duplicate email conflict during seeding
ON CONFLICT (email) DO UPDATE 
-- Updates password hash column on existing record with excluded value
SET password = EXCLUDED.password;

-- 4. Usuario Solicitante de Prueba
-- Email: usuario@campusdesk.com | Password: User2026!
-- Inserts standard test requester user account into users table
INSERT INTO users (first_name, last_name, email, password, active, register_date)
-- Supplies standard user profile details including registration timestamp
VALUES (
    -- Supplies given first name string for sample user
    'Ana',
    -- Supplies family surname string for sample user
    'Gómez',
    -- Supplies unique institutional email for sample user
    'usuario@campusdesk.com',
    -- Stores salted BCrypt cryptographic hash for authentication
    '$2a$10$5uASYI.ltjT0rUza/vbluuMIa9L9E0c6d17yj3nx8jULH.iItt5PK',
    -- Sets user account state active flag to true
    true,
    -- Sets initial registration timestamp to current database timestamp
    NOW()
-- Closes insert values expression for sample user
)
-- Updates existing record on duplicate email conflict during seeding
ON CONFLICT (email) DO UPDATE 
-- Updates password hash column on existing record with excluded value
SET password = EXCLUDED.password;

-- 5. Asignar Roles al SuperAdmin (ADMIN, TECHNICIAN y USER para control total)
-- Inserts user and role association mappings into user_roles join table
INSERT INTO user_roles (user_id, role_id)
-- Selects matching user identifier and role identifiers for administrator
SELECT u.id, r.id
-- Cross joins users and role tables to match target records
FROM users u, role r
-- Filters query to target administrator user email address
WHERE u.email = 'admin@campusdesk.com'
  -- Restricts role assignment to all three fundamental authorization roles
  AND r.name IN ('ADMIN', 'TECHNICIAN', 'USER')
-- Prevents duplicate assignment error if mappings already exist
ON CONFLICT DO NOTHING;

-- 6. Asignar Rol TECHNICIAN al Técnico de Soporte
-- Inserts user and role association mapping for technician user
INSERT INTO user_roles (user_id, role_id)
-- Selects matching user identifier and TECHNICIAN role identifier
SELECT u.id, r.id
-- Cross joins users and role tables to match target records
FROM users u, role r
-- Filters query to target technician user email address
WHERE u.email = 'tecnico@campusdesk.com'
  -- Restricts role assignment exclusively to the TECHNICIAN role
  AND r.name = 'TECHNICIAN'
-- Prevents duplicate assignment error if mapping already exists
ON CONFLICT DO NOTHING;

-- 7. Asignar Rol USER a la colaboradora solicitante
-- Inserts user and role association mapping for standard campus user
INSERT INTO user_roles (user_id, role_id)
-- Selects matching user identifier and USER role identifier
SELECT u.id, r.id
-- Cross joins users and role tables to match target records
FROM users u, role r
-- Filters query to target standard requester user email address
WHERE u.email = 'usuario@campusdesk.com'
  -- Restricts role assignment exclusively to the standard USER role
  AND r.name = 'USER'
-- Prevents duplicate assignment error if mapping already exists
ON CONFLICT DO NOTHING;
