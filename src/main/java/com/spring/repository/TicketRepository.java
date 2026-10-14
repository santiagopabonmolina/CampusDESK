package com.spring.repository;

import com.spring.entity.Ticket;
import com.spring.entity.enums.TicketCategory;
import com.spring.entity.enums.TicketPriority;
import com.spring.entity.enums.TicketState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    // Búsqueda con filtros dinámicos y carga impaciente (FETCH) para evitar LazyInitializationException y N+1
    @Query("SELECT t FROM Ticket t " +
           "JOIN FETCH t.requester " +
           "LEFT JOIN FETCH t.technicianAssigned " +
           "WHERE (:requesterId IS NULL OR t.requester.id = :requesterId) " +
           "AND (:technicianId IS NULL OR t.technicianAssigned.id = :technicianId) " +
           "AND (:state IS NULL OR t.state = :state) " +
           "AND (:priority IS NULL OR t.priority = :priority) " +
           "AND (:category IS NULL OR t.category = :category) " +
           "ORDER BY t.id DESC")
    List<Ticket> findFilteredTickets(
            @Param("requesterId") Long requesterId,
            @Param("technicianId") Long technicianId,
            @Param("state") TicketState state,
            @Param("priority") TicketPriority priority,
            @Param("category") TicketCategory category);

    @Query("SELECT t FROM Ticket t " +
           "JOIN FETCH t.requester " +
           "LEFT JOIN FETCH t.technicianAssigned " +
           "WHERE t.id = :id")
    Optional<Ticket> findByIdWithDetails(@Param("id") Long id);
}
