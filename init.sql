-- ========================================================
-- CampusDESK - Script de Inicialización y Esquema de Base de Datos
-- PostgreSQL
-- ========================================================

-- 1. Tabla de Roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 2. Tabla de Usuarios (incluyendo columna verified para verificación por código)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(150) NOT NULL,
    last_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    register_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. Tabla Intermedia Usuario-Roles
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- 4. Tabla de Verificación de Correo (Tarea 2)
CREATE TABLE IF NOT EXISTS email_verifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    code_hash VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    failed_attempts INT NOT NULL DEFAULT 0,
    last_resend_at TIMESTAMP,
    invalidated BOOLEAN NOT NULL DEFAULT FALSE
);

-- 5. Tabla de Tickets
CREATE TABLE IF NOT EXISTS ticket (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(225) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(30) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    state VARCHAR(30) NOT NULL DEFAULT 'OPENED',
    requester_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    technician_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolution_date TIMESTAMP,
    close_date TIMESTAMP
);

-- 6. Tabla de Comentarios
CREATE TABLE IF NOT EXISTS comment (
    id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ticket_id BIGINT NOT NULL REFERENCES ticket(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 7. Tabla de Historial / Auditoría de Tickets
CREATE TABLE IF NOT EXISTS ticket_history (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES ticket(id) ON DELETE CASCADE,
    previous_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    changed_by BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ========================================================
-- Datos Semilla (Seeds)
-- ========================================================

-- Roles
INSERT INTO roles (name) VALUES 
    ('ADMIN'),
    ('TECHNICIAN'),
    ('USER')
ON CONFLICT (name) DO NOTHING;

-- Usuarios Iniciales ADMIN y TECHNICIAN (con verified = TRUE)
-- Password cifrada con BCrypt: $2a$10$7R9rG2mEwT1V1jZqF2eFVuQW1m2i3t4y5u6i7o8p9a0s1d2f3g (ejemplo BCrypt válido)
INSERT INTO users (first_name, last_name, email, password, active, verified, register_date)
VALUES 
    ('Admin', 'CampusDESK', 'admin@campusdesk.com', '$2a$10$wB5a1wYn1sU2rY9z9jX5c.L4E3U0Z6o4g8e0C3a2A1b0C9d8E7f6a', TRUE, TRUE, CURRENT_TIMESTAMP),
    ('Carlos', 'Técnico', 'tecnico@campusdesk.com', '$2a$10$wB5a1wYn1sU2rY9z9jX5c.L4E3U0Z6o4g8e0C3a2A1b0C9d8E7f6a', TRUE, TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO UPDATE SET verified = TRUE;

-- Asignar Rol ADMIN a admin@campusdesk.com
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'admin@campusdesk.com' AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- Asignar Rol TECHNICIAN a tecnico@campusdesk.com
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'tecnico@campusdesk.com' AND r.name = 'TECHNICIAN'
ON CONFLICT DO NOTHING;
