
-- 1. Catálogo oficial de Roles
INSERT INTO role (name)
VALUES ('ADMIN'), ('TECHNICIAN'), ('USER')
ON CONFLICT (name) DO NOTHING;

-- 2. Usuario Super Administrador
-- Contraseña en texto plano: SuperAdmin2026!
-- Hash BCrypt: $2a$10$IUMAI5JsSqbY5YETYIapJOcTgRRn3qBrEstoLx6HB.aBTL/q7qfg2
INSERT INTO users (first_name, last_name, email, password, active, register_date)
VALUES (
    'Super',
    'Admin',
    'admin@campusdesk.com',
    '$2a$10$IUMAI5JsSqbY5YETYIapJOcTgRRn3qBrEstoLx6HB.aBTL/q7qfg2',
    true,
    NOW()
)
ON CONFLICT (email) DO UPDATE 
SET password = EXCLUDED.password;

-- 3. Usuario Técnico de Soporte
-- Contraseña en texto plano: Tech2026!
-- Hash BCrypt: $2a$10$4JB0UpDme52AL6F8uip7W.aWUT1Vm50T7I0H9UskRqPm3A1.kOXEm
INSERT INTO users (first_name, last_name, email, password, active, register_date)
VALUES (
    'Carlos',
    'Soporte',
    'tecnico@campusdesk.com',
    '$2a$10$4JB0UpDme52AL6F8uip7W.aWUT1Vm50T7I0H9UskRqPm3A1.kOXEm',
    true,
    NOW()
)
ON CONFLICT (email) DO UPDATE 
SET password = EXCLUDED.password;

-- 4. Asignar Roles al SuperAdmin (ADMIN, TECHNICIAN y USER para control total)
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, role r
WHERE u.email = 'admin@campusdesk.com'
  AND r.name IN ('ADMIN', 'TECHNICIAN', 'USER')
ON CONFLICT DO NOTHING;

-- 5. Asignar Rol TECHNICIAN al Técnico
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, role r
WHERE u.email = 'tecnico@campusdesk.com'
  AND r.name = 'TECHNICIAN'
ON CONFLICT DO NOTHING;
