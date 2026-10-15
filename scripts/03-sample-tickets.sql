-- ==============================================================================
-- 03-sample-tickets.sql
-- CampusDESK - TechNova Solutions
-- DML: Casos de prueba y tickets de ejemplo en diferentes estados
-- Idempotente: Si ya existen los tickets de prueba, no se duplican.
-- ==============================================================================

-- Executes an anonymous PL/pgSQL procedural block for sample data creation
DO $$
-- Declares local variables holding primary key identifiers of users and tickets
DECLARE
    -- Stores the unique identifier of the administrator user account
    v_admin_id BIGINT;
    -- Stores the unique identifier of the technician user account
    v_tech_id BIGINT;
    -- Stores the unique identifier of the requester user account
    v_user_id BIGINT;
    -- Stores the generated identifier of the first sample ticket
    v_ticket_1 BIGINT;
    -- Stores the generated identifier of the second sample ticket
    v_ticket_2 BIGINT;
    -- Stores the generated identifier of the third sample ticket
    v_ticket_3 BIGINT;
    -- Stores the generated identifier of the fourth sample ticket
    v_ticket_4 BIGINT;
    -- Stores the generated identifier of the fifth sample ticket
    v_ticket_5 BIGINT;
-- Initiates the execution section of this anonymous procedural block
BEGIN
    -- Evitar duplicados si ya existen tickets de prueba cargados
    -- Checks whether test tickets were already loaded into ticket table
    IF EXISTS (SELECT 1 FROM ticket WHERE title = 'Falla en el teclado mecánico oficina 302') THEN
        -- Emits an informational notice indicating tickets were previously inserted
        RAISE NOTICE 'Los tickets de prueba ya se encuentran cargados en la base de datos.';
        -- Terminates execution of block early avoiding duplicate insertions
        RETURN;
    -- Closes duplicate check condition block
    END IF;

    -- Obtener IDs de usuarios
    -- Retrieves database primary key for super administrator user account
    SELECT id INTO v_admin_id FROM users WHERE email = 'admin@campusdesk.com';
    -- Retrieves database primary key for technical support user account
    SELECT id INTO v_tech_id FROM users WHERE email = 'tecnico@campusdesk.com';
    -- Retrieves database primary key for test requester user account
    SELECT id INTO v_user_id FROM users WHERE email = 'usuario@campusdesk.com';

    -- Verifies that required user accounts exist before inserting sample tickets
    IF v_user_id IS NOT NULL AND v_tech_id IS NOT NULL THEN

        -- Ticket 1: Estado OPENED (Abierta, sin técnico asignado)
        -- Inserts initial sample ticket in OPENED status without technician assigned
        INSERT INTO ticket (title, description, category, priority, state, requester_id, technician_id, create_date)
        -- Supplies ticket field values with creation timestamp set three days ago
        VALUES ('Falla en el teclado mecánico oficina 302', 'Varias teclas no responden, impidiendo el trabajo normal.', 'HARDWARE', 'LOW', 'OPENED', v_user_id, NULL, NOW() - INTERVAL '3 days')
        -- Captures newly generated ticket primary key into local variable
        RETURNING id INTO v_ticket_1;

        -- Inserts initial creation state transition into ticket history audit table
        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        -- Records state transition from null to OPENED by requester user
        VALUES (v_ticket_1, NULL, 'OPENED', v_user_id, NOW() - INTERVAL '3 days');

        -- Ticket 2: Estado ASSIGNED (Asignada a técnico por el Administrador)
        -- Inserts second sample ticket in ASSIGNED status with assigned technician
        INSERT INTO ticket (title, description, category, priority, state, requester_id, technician_id, create_date)
        -- Supplies ticket field values with creation timestamp set two days ago
        VALUES ('Error al conectar a base de datos PostgreSQL', 'Timeout recurrente al intentar conectar a la base de datos de desarrollo.', 'SOFTWARE', 'HIGH', 'ASSIGNED', v_user_id, v_tech_id, NOW() - INTERVAL '2 days')
        -- Captures newly generated ticket primary key into local variable
        RETURNING id INTO v_ticket_2;

        -- Records initial OPENED state creation entry in ticket history audit log
        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        -- Logs transition to OPENED status by requesting user two days ago
        VALUES (v_ticket_2, NULL, 'OPENED', v_user_id, NOW() - INTERVAL '2 days');
        -- Records technician assignment transition in ticket history audit log table
        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        -- Logs transition from OPENED to ASSIGNED status by administrator user
        VALUES (v_ticket_2, 'OPENED', 'ASSIGNED', v_admin_id, NOW() - INTERVAL '2 days' + INTERVAL '2 hours');

        -- Inserts administrative assignment note comment associated with second ticket
        INSERT INTO comment (content, author_id, ticket_id, created_at)
        -- Supplies comment text, administrator author ID, ticket ID, and timestamp
        VALUES ('He asignado este caso al técnico Carlos para su diagnóstico.', v_admin_id, v_ticket_2, NOW() - INTERVAL '2 days' + INTERVAL '2 hours');

        -- Ticket 3: Estado IN_PROCESS (En atención por el técnico)
        -- Inserts third sample ticket in IN_PROCESS status under active investigation
        INSERT INTO ticket (title, description, category, priority, state, requester_id, technician_id, create_date)
        -- Supplies ticket field values with critical priority created one day ago
        VALUES ('Caída de señal WiFi en piso 4', 'La red corporativa intermitente afecta a todo el equipo de ventas.', 'NETWORK', 'CRITIC', 'IN_PROCESS', v_user_id, v_tech_id, NOW() - INTERVAL '1 day')
        -- Captures newly generated ticket primary key into local variable
        RETURNING id INTO v_ticket_3;

        -- Records initial creation entry in ticket history audit log table
        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        -- Logs transition to OPENED status by requesting user one day ago
        VALUES (v_ticket_3, NULL, 'OPENED', v_user_id, NOW() - INTERVAL '1 day');
        -- Records technician assignment transition in ticket history audit log table
        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        -- Logs transition from OPENED to ASSIGNED status by administrator user
        VALUES (v_ticket_3, 'OPENED', 'ASSIGNED', v_admin_id, NOW() - INTERVAL '22 hours');
        -- Records diagnosis start transition in ticket history audit log table
        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        -- Logs transition from ASSIGNED to IN_PROCESS status by technician user
        VALUES (v_ticket_3, 'ASSIGNED', 'IN_PROCESS', v_tech_id, NOW() - INTERVAL '20 hours');

        -- Inserts diagnostic progress update comment posted by support technician user
        INSERT INTO comment (content, author_id, ticket_id, created_at)
        -- Supplies diagnostic comment text, technician author ID, and timestamp
        VALUES ('Iniciando revisión del Access Point ubicado en el pasillo norte.', v_tech_id, v_ticket_3, NOW() - INTERVAL '20 hours');

        -- Ticket 4: Estado RESOLVED (Resuelta, esperando confirmación de cierre por el solicitante)
        -- Inserts fourth sample ticket in RESOLVED status with resolution timestamp set
        INSERT INTO ticket (title, description, category, priority, state, requester_id, technician_id, create_date, resolution_date)
        -- Supplies ticket field values with creation and resolution timestamps recorded
        VALUES ('Restablecimiento de contraseña VPN', 'No puedo acceder al entorno de trabajo remoto desde casa.', 'ACCESS', 'MEDIUM', 'RESOLVED', v_user_id, v_tech_id, NOW() - INTERVAL '18 hours', NOW() - INTERVAL '2 hours')
        -- Captures newly generated ticket primary key into local variable
        RETURNING id INTO v_ticket_4;

        -- Records initial OPENED state creation entry in ticket history audit log
        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        -- Logs transition to OPENED status by requesting user eighteen hours ago
        VALUES (v_ticket_4, NULL, 'OPENED', v_user_id, NOW() - INTERVAL '18 hours');
        -- Records technician assignment transition in ticket history audit log table
        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        -- Logs transition from OPENED to ASSIGNED status by administrator user
        VALUES (v_ticket_4, 'OPENED', 'ASSIGNED', v_admin_id, NOW() - INTERVAL '16 hours');
        -- Records diagnosis start transition in ticket history audit log table
        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        -- Logs transition from ASSIGNED to IN_PROCESS status by technician user
        VALUES (v_ticket_4, 'ASSIGNED', 'IN_PROCESS', v_tech_id, NOW() - INTERVAL '14 hours');
        -- Records problem resolution transition in ticket history audit log table
        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        -- Logs transition from IN_PROCESS to RESOLVED status by technician user
        VALUES (v_ticket_4, 'IN_PROCESS', 'RESOLVED', v_tech_id, NOW() - INTERVAL '2 hours');

        -- Inserts resolution instruction comment notifying user to confirm closure
        INSERT INTO comment (content, author_id, ticket_id, created_at)
        -- Supplies resolution note text, technician author ID, and timestamp
        VALUES ('Contraseña temporal restablecida y enviada al correo corporativo. Por favor confirma el cierre.', v_tech_id, v_ticket_4, NOW() - INTERVAL '2 hours');

        -- Ticket 5: Estado CLOSED (Cerrada por la solicitante)
        -- Inserts fifth sample ticket in fully closed status with closing timestamp
        INSERT INTO ticket (title, description, category, priority, state, requester_id, technician_id, create_date, resolution_date, close_date)
        -- Supplies ticket field values with complete lifecycle progression timestamps populated
        VALUES ('Solicitud de monitor secundario', 'Instalación de monitor HDMI adicional para estación de desarrollo.', 'HARDWARE', 'LOW', 'CLOSED', v_user_id, v_tech_id, NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days' + INTERVAL '3 hours')
        -- Captures newly generated ticket primary key into local variable
        RETURNING id INTO v_ticket_5;

        -- Records initial creation entry in ticket history audit log table
        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        -- Logs transition to OPENED status by requesting user five days ago
        VALUES (v_ticket_5, NULL, 'OPENED', v_user_id, NOW() - INTERVAL '5 days');
        -- Records technician assignment transition in ticket history audit log table
        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        -- Logs transition from OPENED to ASSIGNED status by administrator user
        VALUES (v_ticket_5, 'OPENED', 'ASSIGNED', v_admin_id, NOW() - INTERVAL '5 days' + INTERVAL '1 hour');
        -- Records work start transition in ticket history audit log table
        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        -- Logs transition from ASSIGNED to IN_PROCESS status by technician user
        VALUES (v_ticket_5, 'ASSIGNED', 'IN_PROCESS', v_tech_id, NOW() - INTERVAL '5 days' + INTERVAL '2 hours');
        -- Records technical resolution transition in ticket history audit log table
        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        -- Logs transition from IN_PROCESS to RESOLVED status by technician user
        VALUES (v_ticket_5, 'IN_PROCESS', 'RESOLVED', v_tech_id, NOW() - INTERVAL '4 days');
        -- Records user closing confirmation transition in ticket history audit log table
        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        -- Logs transition from RESOLVED to CLOSED status by requester user
        VALUES (v_ticket_5, 'RESOLVED', 'CLOSED', v_user_id, NOW() - INTERVAL '4 days' + INTERVAL '3 hours');

        -- Inserts equipment delivery confirmation comment posted by technician user
        INSERT INTO comment (content, author_id, ticket_id, created_at)
        -- Supplies technician delivery confirmation comment text and author details
        VALUES ('Monitor entregado e instalado correctamente.', v_tech_id, v_ticket_5, NOW() - INTERVAL '4 days');
        -- Inserts requester satisfaction note comment confirming closure of ticket
        INSERT INTO comment (content, author_id, ticket_id, created_at)
        -- Supplies requester gratitude comment text and user author details
        VALUES ('Todo funciona perfecto, gracias.', v_user_id, v_ticket_5, NOW() - INTERVAL '4 days' + INTERVAL '3 hours');

    -- Closes user existence condition block
    END IF;
-- Closes execution block of anonymous procedure
END $$;
