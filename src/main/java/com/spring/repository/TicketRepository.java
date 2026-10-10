package com.spring.repository;

import com.spring.entity.Ticket;
import com.spring.entity.enums.TicketCategory;
import com.spring.entity.enums.TicketPriority;
import com.spring.entity.enums.TicketState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Tickets creados por un usuario solicitante
    List<Ticket> findByRequesterId(Long requesterId);

    // Tickets asignados a un técnico
    List<Ticket> findByTechnicianAssignedId(Long technicianId);

    // Filtrar por estado (OPENED, ASSIGNED, IN_PROCESS, etc.)
    List<Ticket> findByState(TicketState state);

    // Filtrar por prioridad o categoría
    List<Ticket> findByPriority(TicketPriority priority);
    List<Ticket> findByCategory(TicketCategory category);

    // Contadores para el Dashboard
    long countByState(TicketState state);
    long countByPriority(TicketPriority priority);
}