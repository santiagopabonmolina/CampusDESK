// Defines the package namespace containing Spring Data JPA repository interfaces
package com.spring.repository;

// Imports the Ticket entity managed by this repository
import com.spring.entity.Ticket;
// Imports the TicketCategory enum for filtering tickets by incident category
import com.spring.entity.enums.TicketCategory;
// Imports the TicketPriority enum for filtering tickets by urgency priority
import com.spring.entity.enums.TicketPriority;
// Imports the TicketState enum for filtering tickets by workflow state
import com.spring.entity.enums.TicketState;
// Imports JpaRepository providing CRUD, sorting, and pagination database operations
import org.springframework.data.jpa.repository.JpaRepository;
// Imports Spring stereotype repository annotation enabling Spring Data bean detection
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// Imports Java utility List collection interface for returning query result lists
import java.util.List;

// Marks this interface as a Spring Data repository component bean
@Repository
// Declares repository interface extending JpaRepository for Ticket entity persistence
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * Finds tickets by requester.
     */
    // Queries all tickets created by a specific user identified by requester ID
    List<Ticket> findByRequesterId(Long requesterId);

    /**
     * Finds requester tickets state.
     */
    // Queries tickets created by a requester filtered by specific lifecycle state
    List<Ticket> findByRequesterIdAndState(Long requesterId, TicketState state);

    /**
     * Finds technician assigned tickets.
     */
    // Queries all tickets currently assigned to a specific technician user ID
    List<Ticket> findByTechnicianAssignedId(Long technicianId);

    /**
     * Finds technician tickets state.
     */
    // Queries tickets assigned to a technician filtered by specific lifecycle state
    List<Ticket> findByTechnicianAssignedIdAndState(Long technicianId, TicketState state);

    /**
     * Finds tickets by state.
     */
    // Queries all tickets across the system having a given lifecycle state
    List<Ticket> findByState(TicketState state);

    /**
     * Finds tickets by priority.
     */
    // Queries all tickets across the system matching a specified priority level
    List<Ticket> findByPriority(TicketPriority priority);

    /**
     * Finds tickets by category.
     */
    // Queries all tickets across the system categorized under a specified category
    List<Ticket> findByCategory(TicketCategory category);

    /**
     * Counts tickets by state.
     */
    // Computes aggregate total count of tickets currently in a given state
    long countByState(TicketState state);

    /**
     * Counts tickets by priority.
     */
    // Computes aggregate total count of tickets classified with a given priority
    long countByPriority(TicketPriority priority);

    /**
     * Counts tickets by requester.
     */
    // Computes aggregate total count of tickets created by a specific user
    long countByRequesterId(Long requesterId);

    /**
     * Counts requester tickets state.
     */
    // Computes aggregate count of tickets created by requester matching a state
    long countByRequesterIdAndState(Long requesterId, TicketState state);

    /**
     * Counts technician assigned tickets.
     */
    // Computes aggregate total count of tickets assigned to a designated technician
    long countByTechnicianAssignedId(Long technicianId);

    /**
     * Counts technician tickets state.
     */
    // Computes count of tickets assigned to a technician matching a state
    long countByTechnicianAssignedIdAndState(Long technicianId, TicketState state);

    @Query("SELECT DISTINCT t FROM Ticket t JOIN t.tags tag WHERE UPPER(tag.nombre) = UPPER(:nombre)")
    List<Ticket> findByTagNombreIgnoreCase(@Param("nombre") String nombre);


// Closes the TicketRepository interface definition
}
