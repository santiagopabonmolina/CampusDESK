-- ==============================================================================
-- 03-sample-tickets.sql
-- CampusDESK - TechNova Solutions
-- DML: Casos de prueba y tickets de ejemplo en diferentes estados
-- ==============================================================================

DO $$
DECLARE
    v_admin_id BIGINT;
    v_tech_id BIGINT;
    v_user_id BIGINT;
    v_ticket_1 BIGINT;
    v_ticket_2 BIGINT;
    v_ticket_3 BIGINT;
    v_ticket_4 BIGINT;
    v_ticket_5 BIGINT;
BEGIN
    -- Obtener IDs de usuarios
    SELECT id INTO v_admin_id FROM users WHERE email = 'admin@campusdesk.com';
    SELECT id INTO v_tech_id FROM users WHERE email = 'tecnico@campusdesk.com';
    SELECT id INTO v_user_id FROM users WHERE email = 'usuario@campusdesk.com';

    IF v_user_id IS NOT NULL AND v_tech_id IS NOT NULL THEN

        -- Ticket 1: Estado OPENED (Abierta, sin técnico)
        INSERT INTO ticket (title, description, category, priority, state, requester_id, technician_id, create_date)
        VALUES ('Falla en el teclado mecánico oficina 302', 'Varias teclas no responden, impidiendo el trabajo normal.', 'HARDWARE', 'LOW', 'OPENED', v_user_id, NULL, NOW() - INTERVAL '3 days')
        RETURNING id INTO v_ticket_1;

        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        VALUES (v_ticket_1, NULL, 'OPENED', v_user_id, NOW() - INTERVAL '3 days');

        -- Ticket 2: Estado ASSIGNED (Asignada a técnico)
        INSERT INTO ticket (title, description, category, priority, state, requester_id, technician_id, create_date)
        VALUES ('Error al conectar a base de datos PostgreSQL', 'Timeout recurrente al intentar conectar a la base de datos de desarrollo.', 'SOFTWARE', 'HIGH', 'ASSIGNED', v_user_id, v_tech_id, NOW() - INTERVAL '2 days')
        RETURNING id INTO v_ticket_2;

        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        VALUES (v_ticket_2, NULL, 'OPENED', v_user_id, NOW() - INTERVAL '2 days');
        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        VALUES (v_ticket_2, 'OPENED', 'ASSIGNED', v_admin_id, NOW() - INTERVAL '2 days' + INTERVAL '2 hours');

        INSERT INTO comment (content, author_id, ticket_id, created_at)
        VALUES ('He asignado este caso al técnico Carlos para su diagnóstico.', v_admin_id, v_ticket_2, NOW() - INTERVAL '2 days' + INTERVAL '2 hours');

        -- Ticket 3: Estado IN_PROCESS (En atención por el técnico)
        INSERT INTO ticket (title, description, category, priority, state, requester_id, technician_id, create_date)
        VALUES ('Caída de señal WiFi en piso 4', 'La red corporativa intermitente afecta a todo el equipo de ventas.', 'NETWORK', 'CRITIC', 'IN_PROCESS', v_user_id, v_tech_id, NOW() - INTERVAL '1 day')
        RETURNING id INTO v_ticket_3;

        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        VALUES (v_ticket_3, NULL, 'OPENED', v_user_id, NOW() - INTERVAL '1 day');
        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        VALUES (v_ticket_3, 'OPENED', 'ASSIGNED', v_admin_id, NOW() - INTERVAL '22 hours');
        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        VALUES (v_ticket_3, 'ASSIGNED', 'IN_PROCESS', v_tech_id, NOW() - INTERVAL '20 hours');

        INSERT INTO comment (content, author_id, ticket_id, created_at)
        VALUES ('Iniciando revisión del Access Point ubicado en el pasillo norte.', v_tech_id, v_ticket_3, NOW() - INTERVAL '20 hours');

        -- Ticket 4: Estado RESOLVED (Resuelta, esperando confirmación del solicitante)
        INSERT INTO ticket (title, description, category, priority, state, requester_id, technician_id, create_date, resolution_date)
        VALUES ('Restablecimiento de contraseña VPN', 'No puedo acceder al entorno de trabajo remoto desde casa.', 'ACCESS', 'MEDIUM', 'RESOLVED', v_user_id, v_tech_id, NOW() - INTERVAL '18 hours', NOW() - INTERVAL '2 hours')
        RETURNING id INTO v_ticket_4;

        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        VALUES (v_ticket_4, NULL, 'OPENED', v_user_id, NOW() - INTERVAL '18 hours');
        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        VALUES (v_ticket_4, 'OPENED', 'ASSIGNED', v_admin_id, NOW() - INTERVAL '16 hours');
        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        VALUES (v_ticket_4, 'ASSIGNED', 'IN_PROCESS', v_tech_id, NOW() - INTERVAL '14 hours');
        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        VALUES (v_ticket_4, 'IN_PROCESS', 'RESOLVED', v_tech_id, NOW() - INTERVAL '2 hours');

        INSERT INTO comment (content, author_id, ticket_id, created_at)
        VALUES ('Contraseña temporal restablecida y enviada al correo corporativo. Por favor confirma el cierre.', v_tech_id, v_ticket_4, NOW() - INTERVAL '2 hours');

        -- Ticket 5: Estado CLOSED (Cerrada por el solicitante)
        INSERT INTO ticket (title, description, category, priority, state, requester_id, technician_id, create_date, resolution_date, close_date)
        VALUES ('Solicitud de monitor secundario', 'Instalación de monitor HDMI adicional para estación de desarrollo.', 'HARDWARE', 'LOW', 'CLOSED', v_user_id, v_tech_id, NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days' + INTERVAL '3 hours')
        RETURNING id INTO v_ticket_5;

        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        VALUES (v_ticket_5, NULL, 'OPENED', v_user_id, NOW() - INTERVAL '5 days');
        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        VALUES (v_ticket_5, 'OPENED', 'ASSIGNED', v_admin_id, NOW() - INTERVAL '5 days' + INTERVAL '1 hour');
        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        VALUES (v_ticket_5, 'ASSIGNED', 'IN_PROCESS', v_tech_id, NOW() - INTERVAL '5 days' + INTERVAL '2 hours');
        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        VALUES (v_ticket_5, 'IN_PROCESS', 'RESOLVED', v_tech_id, NOW() - INTERVAL '4 days');
        INSERT INTO ticket_history (ticket_id, previous_status, new_status, changed_by, changed_at)
        VALUES (v_ticket_5, 'RESOLVED', 'CLOSED', v_user_id, NOW() - INTERVAL '4 days' + INTERVAL '3 hours');

        INSERT INTO comment (content, author_id, ticket_id, created_at)
        VALUES ('Monitor entregado e instalado correctamente.', v_tech_id, v_ticket_5, NOW() - INTERVAL '4 days');
        INSERT INTO comment (content, author_id, ticket_id, created_at)
        VALUES ('Todo funciona perfecto, gracias.', v_user_id, v_ticket_5, NOW() - INTERVAL '4 days' + INTERVAL '3 hours');

    END IF;
END $$;
