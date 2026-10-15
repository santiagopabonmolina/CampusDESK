-- ==============================================================================
-- 01-schema.sql
-- CampusDESK - TechNova Solutions
-- DDL: Definición de estructura de tablas, relaciones y restricciones
-- ==============================================================================

-- 1. Tabla de Roles del Sistema
-- Creates the role table storing security roles if it does not already exist
CREATE TABLE IF NOT EXISTS role (
    -- Auto-incrementing 64-bit integer serving as primary key for role
    id BIGSERIAL PRIMARY KEY,
    -- Unique varchar column storing role name string with length twenty
    name VARCHAR(20) NOT NULL UNIQUE
-- Closes role table definition
);

-- 2. Tabla de Usuarios y Colaboradores
-- Creates the users table storing accounts if it does not already exist
CREATE TABLE IF NOT EXISTS users (
    -- Auto-incrementing 64-bit integer serving as primary key for user
    id BIGSERIAL PRIMARY KEY,
    -- Required varchar column storing user first name up to 150 characters
    first_name VARCHAR(150) NOT NULL,
    -- Required varchar column storing user family surname up to 150 characters
    last_name VARCHAR(150) NOT NULL,
    -- Unique required varchar column storing institutional user email for login
    email VARCHAR(150) NOT NULL UNIQUE,
    -- Required varchar column storing salted BCrypt password hash string
    password VARCHAR(255) NOT NULL,
    -- Boolean flag indicating active status defaulting to true on creation
    active BOOLEAN NOT NULL DEFAULT true,
    -- Timestamp column recording registration date defaulting to current database time
    register_date TIMESTAMP NOT NULL DEFAULT NOW()
-- Closes users table definition
);

-- 3. Tabla intermedia: Roles por Usuario (Muchos a Muchos)
-- Creates join table linking users and roles in many-to-many relationship
CREATE TABLE IF NOT EXISTS user_roles (
    -- Foreign key integer referencing primary key of users table
    user_id BIGINT NOT NULL,
    -- Foreign key integer referencing primary key of role table
    role_id BIGINT NOT NULL,
    -- Composite primary key enforcing unique user and role associations
    PRIMARY KEY (user_id, role_id),
    -- Cascades delete operations from users table removing orphan mappings
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    -- Cascades delete operations from role table removing orphan mappings
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES role (id) ON DELETE CASCADE
-- Closes user_roles table definition
);

-- 4. Tabla de Tickets e Incidencias Tecnológicas
-- Creates primary ticket table storing support incident records and status
CREATE TABLE IF NOT EXISTS ticket (
    -- Auto-incrementing 64-bit integer serving as primary key for ticket
    id BIGSERIAL PRIMARY KEY,
    -- Required varchar column storing concise incident title up to 225 characters
    title VARCHAR(225) NOT NULL,
    -- Required text column storing detailed description of the reported issue
    description TEXT NOT NULL,
    -- Required varchar column storing ticket domain category classification
    category VARCHAR(30) NOT NULL,
    -- Required varchar column storing ticket urgency priority rating
    priority VARCHAR(20) NOT NULL,
    -- Required varchar column storing ticket workflow lifecycle progression state
    state VARCHAR(30) NOT NULL,
    -- Required foreign key integer referencing requester in users table
    requester_id BIGINT NOT NULL,
    -- Optional foreign key integer referencing assigned technician in users table
    technician_id BIGINT,
    -- Creation timestamp recording when ticket was submitted defaulting to now
    create_date TIMESTAMP NOT NULL DEFAULT NOW(),
    -- Optional timestamp populated when technician marks ticket as resolved
    resolution_date TIMESTAMP,
    -- Optional timestamp populated when requester confirms closure of ticket
    close_date TIMESTAMP,
    -- Foreign key constraint linking requester_id to users primary key
    CONSTRAINT fk_ticket_requester FOREIGN KEY (requester_id) REFERENCES users (id),
    -- Foreign key constraint linking technician_id to users primary key
    CONSTRAINT fk_ticket_technician FOREIGN KEY (technician_id) REFERENCES users (id)
-- Closes ticket table definition
);

-- 5. Tabla de Comentarios en Tickets
-- Creates comment table storing conversation thread notes on support tickets
CREATE TABLE IF NOT EXISTS comment (
    -- Auto-incrementing 64-bit integer serving as primary key for comment
    id BIGSERIAL PRIMARY KEY,
    -- Required text column storing message body content of comment
    content TEXT NOT NULL,
    -- Foreign key integer referencing author user in users table
    author_id BIGINT NOT NULL,
    -- Foreign key integer referencing parent ticket in ticket table
    ticket_id BIGINT NOT NULL,
    -- Creation timestamp recording when comment was posted defaulting to now
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    -- Foreign key constraint linking author_id to users primary key
    CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES users (id),
    -- Cascades ticket deletion removing associated comments from database table
    CONSTRAINT fk_comment_ticket FOREIGN KEY (ticket_id) REFERENCES ticket (id) ON DELETE CASCADE
-- Closes comment table definition
);

-- 6. Tabla de Historial y Auditoría de Cambios de Estado
-- Creates immutable audit log table recording all ticket state transitions
CREATE TABLE IF NOT EXISTS ticket_history (
    -- Auto-incrementing 64-bit integer serving as primary key for history
    id BIGSERIAL PRIMARY KEY,
    -- Foreign key integer referencing affected ticket in ticket table
    ticket_id BIGINT NOT NULL,
    -- Optional varchar column storing status prior to transition occurring
    previous_status VARCHAR(30),
    -- Required varchar column storing new status resulting from transition
    new_status VARCHAR(30) NOT NULL,
    -- Foreign key integer referencing actor user who initiated transition
    changed_by BIGINT NOT NULL,
    -- Timestamp recording when transition occurred defaulting to current time
    changed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    -- Cascades ticket deletion removing associated audit records from database
    CONSTRAINT fk_history_ticket FOREIGN KEY (ticket_id) REFERENCES ticket (id) ON DELETE CASCADE,
    -- Foreign key constraint linking changed_by to users primary key
    CONSTRAINT fk_history_changed_by FOREIGN KEY (changed_by) REFERENCES users (id)
-- Closes ticket_history table definition
);

-- Índices para optimización de consultas frecuentes
-- Creates B-tree database index accelerating queries filtering tickets by state
CREATE INDEX IF NOT EXISTS idx_ticket_state ON ticket(state);
-- Creates B-tree database index accelerating queries filtering tickets by requester
CREATE INDEX IF NOT EXISTS idx_ticket_requester ON ticket(requester_id);
-- Creates B-tree database index accelerating queries filtering tickets by technician
CREATE INDEX IF NOT EXISTS idx_ticket_technician ON ticket(technician_id);
-- Creates B-tree database index accelerating queries loading comments by ticket ID
CREATE INDEX IF NOT EXISTS idx_comment_ticket ON comment(ticket_id);
-- Creates B-tree database index accelerating queries loading history by ticket ID
CREATE INDEX IF NOT EXISTS idx_history_ticket ON ticket_history(ticket_id);
