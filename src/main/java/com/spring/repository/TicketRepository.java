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

    /**
     * Finds tickets by requester.
     */
    List<Ticket> findByRequesterId(Long requesterId);

    /**
     * Finds requester tickets state.
     */
    List<Ticket> findByRequesterIdAndState(Long requesterId, TicketState state);

    /**
     * Finds technician assigned tickets.
     */
    List<Ticket> findByTechnicianAssignedId(Long technicianId);

    /**
     * Finds technician tickets state.
     */
    List<Ticket> findByTechnicianAssignedIdAndState(Long technicianId, TicketState state);

    /**
     * Finds tickets by state.
     */
    List<Ticket> findByState(TicketState state);

    /**
     * Finds tickets by priority.
     */
    List<Ticket> findByPriority(TicketPriority priority);

    /**
     * Finds tickets by category.
     */
    List<Ticket> findByCategory(TicketCategory category);

    /**
     * Counts tickets by state.
     */
    long countByState(TicketState state);

    /**
     * Counts tickets by priority.
     */
    long countByPriority(TicketPriority priority);

    /**
     * Counts tickets by requester.
     */
    long countByRequesterId(Long requesterId);

    /**
     * Counts requester tickets state.
     */
    long countByRequesterIdAndState(Long requesterId, TicketState state);

    /**
     * Counts technician assigned tickets.
     */
    long countByTechnicianAssignedId(Long technicianId);

    /**
     * Counts technician tickets state.
     */
    long countByTechnicianAssignedIdAndState(Long technicianId, TicketState state);
}
