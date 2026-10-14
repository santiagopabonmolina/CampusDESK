-- ==============================================================================
-- 01-schema.sql
-- CampusDESK - TechNova Solutions
-- DDL: Definición de estructura de tablas, relaciones y restricciones
-- ==============================================================================

-- 1. Tabla de Roles del Sistema
CREATE TABLE IF NOT EXISTS role (\n    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

-- 2. Tabla de Usuarios y Colaboradores
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(150) NOT NULL,
    last_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    verification_code VARCHAR(10),
    verification_code_expires_at TIMESTAMP,
    register_date TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Migración segura si la tabla users ya existía previamente
ALTER TABLE users ADD COLUMN IF NOT EXISTS verification_code VARCHAR(10);
ALTER TABLE users ADD COLUMN IF NOT EXISTS verification_code_expires_at TIMESTAMP;

-- 3. Tabla intermedia: Roles por Usuario (Muchos a Muchos)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES role (id) ON DELETE CASCADE
);

-- 4. Tabla de Tickets e Incidencias Tecnológicas
CREATE TABLE IF NOT EXISTS ticket (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(225) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(30) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    state VARCHAR(30) NOT NULL,
    requester_id BIGINT NOT NULL,
    technician_id BIGINT,
    create_date TIMESTAMP NOT NULL DEFAULT NOW(),
    resolution_date TIMESTAMP,
    close_date TIMESTAMP,
    CONSTRAINT fk_ticket_requester FOREIGN KEY (requester_id) REFERENCES users (id),
    CONSTRAINT fk_ticket_technician FOREIGN KEY (technician_id) REFERENCES users (id)
);

-- 5. Tabla de Comentarios de Seguimiento
CREATE TABLE IF NOT EXISTS comment (
    id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    ticket_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    create_date TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_comment_ticket FOREIGN KEY (ticket_id) REFERENCES ticket (id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- 6. Tabla de Historial y Auditoría de Estados del Ticket
CREATE TABLE IF NOT EXISTS ticket_history (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    previous_state VARCHAR(30),
    new_state VARCHAR(30) NOT NULL,
    changed_by BIGINT NOT NULL,
    change_date TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_history_ticket FOREIGN KEY (ticket_id) REFERENCES ticket (id) ON DELETE CASCADE,
    CONSTRAINT fk_history_changed_by FOREIGN KEY (changed_by) REFERENCES users (id)
);

-- Índices para optimización de consultas frecuentes
CREATE INDEX IF NOT EXISTS idx_ticket_state ON ticket(state);
CREATE INDEX IF NOT EXISTS idx_ticket_requester ON ticket(requester_id);
CREATE INDEX IF NOT EXISTS idx_ticket_technician ON ticket(technician_id);
CREATE INDEX IF NOT EXISTS idx_comment_ticket ON comment(ticket_id);
CREATE INDEX IF NOT EXISTS idx_history_ticket ON ticket_history(ticket_id);
