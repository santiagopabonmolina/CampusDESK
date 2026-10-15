// Defines the package namespace containing outbound server response DTOs
package com.spring.dto.response;

// Imports the TicketState enum representing prior and updated ticket states
import com.spring.entity.enums.TicketState;
// Imports the TicketHistory entity mapped into this audit response record
import com.spring.entity.TicketHistory;

// Imports Java time class representing transition timestamp date and time
import java.time.LocalDateTime;

// Declares immutable record data transfer object carrying ticket audit transition data
public record TicketHistoryResponse(
        // Declares unique identifier component of this history record
        Long id,
        // Declares ticket identifier component referencing the parent ticket
        Long ticketId,
        // Declares previous status state component prior to transition
        TicketState previousStatus,
        // Declares new updated status state component following transition
        TicketState newStatus,
        // Declares identifier component of user performing this transition
        Long changedById,
        // Declares email component of user performing this status transition
        String changedByEmail,
        // Declares timestamp component recording when transition took place
        LocalDateTime changedAt
// Declares opening body of TicketHistoryResponse record definition
) {

    /**
     * Maps entry to response.
     */
    // Factory method converting TicketHistory JPA entity to TicketHistoryResponse record
    public static TicketHistoryResponse from(TicketHistory entry) {
        // Instantiates and returns a new TicketHistoryResponse mapping attributes from entity
        return new TicketHistoryResponse(
                entry.getId(),
                entry.getTicket().getId(),
                entry.getPreviousStatus(),
                entry.getNewStatus(),
                entry.getChangedBy().getId(),
                entry.getChangedBy().getEmail(),
                entry.getChangedAt()
        );
    // Closes from static factory method
    }
// Closes TicketHistoryResponse record definition
}
