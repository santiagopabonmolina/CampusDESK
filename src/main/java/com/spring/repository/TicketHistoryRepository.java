// Defines the package namespace containing Spring Data JPA repository interfaces
package com.spring.repository;

// Imports the TicketHistory audit entity persisted by this repository
import com.spring.entity.TicketHistory;
// Imports base Spring Data Repository marker interface restricting exposed CRUD methods
import org.springframework.data.repository.Repository;

// Imports Java utility List collection interface for returning query result sets
import java.util.List;
// Imports Optional container class for null-safe query result handling
import java.util.Optional;

/**
 * Extends the marker Repository (not JpaRepository) on purpose, so that
 * delete and update operations are not exposed for historical records.
 */
// Declares immutable history repository interface extending base marker Repository interface
public interface TicketHistoryRepository extends Repository<TicketHistory, Long> {

    /**
     * Saves ticket history record.
     */
    // Persists a new immutable ticket history audit transition record to PostgreSQL
    TicketHistory save(TicketHistory entry);

    /**
     * Finds ticket history records.
     */
    // Queries audit history entries for a ticket sorted chronologically in ascending order
    List<TicketHistory> findByTicketIdOrderByChangedAtAscIdAsc(Long ticketId);

    /**
     * Finds latest ticket history.
     */
    // Queries the single most recent state transition history entry for a ticket
    Optional<TicketHistory> findFirstByTicketIdOrderByChangedAtDescIdDesc(Long ticketId);
// Closes the TicketHistoryRepository interface definition
}
