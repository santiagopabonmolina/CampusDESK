// Defines the package namespace containing business service layer classes
package com.spring.service;

// Imports Ticket entity representing support ticket database records
import com.spring.entity.Ticket;
// Imports TicketRepository providing ticket query and persistence methods
import com.spring.repository.TicketRepository;
// Imports TicketState enum representing prior and updated ticket states
import com.spring.entity.enums.TicketState;
// Imports User entity representing the actor making status changes
import com.spring.entity.User;
// Imports TicketHistory entity representing immutable audit log records
import com.spring.entity.TicketHistory;
// Imports TicketHistoryRepository providing audit log persistence operations
import com.spring.repository.TicketHistoryRepository;
// Imports TicketHistoryResponse record DTO carrying history log data to clients
import com.spring.dto.response.TicketHistoryResponse;
// Imports HttpStatus enum representing standard HTTP status response codes
import org.springframework.http.HttpStatus;
// Imports Spring Security Authentication representing the authenticated user principal
import org.springframework.security.core.Authentication;
// Imports GrantedAuthority interface representing security authorities and roles
import org.springframework.security.core.GrantedAuthority;
// Imports Spring Service annotation declaring this class as business service component
import org.springframework.stereotype.Service;
// Imports Propagation enum defining transaction propagation behavior for operations
import org.springframework.transaction.annotation.Propagation;
// Imports Spring Transactional annotation for managing transactional database boundaries
import org.springframework.transaction.annotation.Transactional;
// Imports ResponseStatusException for throwing HTTP-aware exceptions from services
import org.springframework.web.server.ResponseStatusException;

// Imports Java utility List collection interface for returning sequential collections
import java.util.List;
// Imports Objects utility class providing null-checking validation helper methods
import java.util.Objects;

// Marks this class as Spring service component for managing ticket audit histories
@Service
// Declares TicketHistoryService managing immutable audit logs for ticket state changes
public class TicketHistoryService {

    // Defines constant string for administrator security role authority
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    // Defines constant string for technician security role authority
    private static final String ROLE_TECHNICIAN = "ROLE_TECHNICIAN";

    // Declares repository reference for saving and querying ticket history records
    private final TicketHistoryRepository ticketHistoryRepository;
    // Declares repository reference for looking up ticket entities by identifier
    private final TicketRepository ticketRepository;

    /**
     * Initializes history service instance.
     */
    // Explicit constructor injecting required repository dependencies into service
    public TicketHistoryService(TicketHistoryRepository ticketHistoryRepository,
                                TicketRepository ticketRepository) {
        // Assigns injected TicketHistoryRepository reference to final class field
        this.ticketHistoryRepository = ticketHistoryRepository;
        // Assigns injected TicketRepository reference to final class field
        this.ticketRepository = ticketRepository;
    // Closes constructor definition
    }

    /**
     * Records ticket state transition.
     */
    // Requires an active transaction propagation preventing independent uncommitted audit logs
    @Transactional(propagation = Propagation.MANDATORY)
    // Persists an immutable history transition record validating input non-null constraints
    public TicketHistoryResponse recordChange(Ticket ticket,
                                              TicketState previousStatus,
                                              TicketState newStatus,
                                              User changedBy) {
        // Validates that ticket parameter is not null throwing NullPointerException
        Objects.requireNonNull(ticket, "ticket must not be null");
        // Validates that newStatus parameter is not null throwing NullPointerException
        Objects.requireNonNull(newStatus, "newStatus must not be null");
        // Validates that changedBy user parameter is not null throwing NullPointerException
        Objects.requireNonNull(changedBy, "changedBy must not be null");

        // Verifies that new state differs from previous state before recording
        if (previousStatus == newStatus) {
            // Throws IllegalArgumentException if transition attempts setting identical state
            throw new IllegalArgumentException("Previous and new status must be different");
        // Closes if condition checking status difference
        }

        // Checks whether ticket ID is non-null to prevent duplicate history records
        if (ticket.getId() != null) {
            // Checks whether an identical history entry was just recorded for ticket
            boolean duplicated = ticketHistoryRepository
                    .findFirstByTicketIdOrderByChangedAtDescIdDesc(ticket.getId())
                    .map(last -> last.getNewStatus() == newStatus && last.getPreviousStatus() == previousStatus)
                    .orElse(false);
            // Rejects recording transition if it is an exact duplicate entry
            if (duplicated) {
                // Throws IllegalStateException preventing redundant audit entries in log
                throw new IllegalStateException("Duplicated history entry for the same transition");
            // Closes if condition checking duplicate history
            }
        // Closes if condition checking ticket ID presence
        }

        // Saves new immutable TicketHistory audit entity to PostgreSQL database
        TicketHistory saved = ticketHistoryRepository.save(
                new TicketHistory(ticket, previousStatus, newStatus, changedBy));
        // Converts saved entity to response record and returns it to caller
        return TicketHistoryResponse.from(saved);
    // Closes recordChange method
    }

    /**
     * Retrieves ticket audit log.
     */
    // Configures read-only transaction optimizing performance for querying history records
    @Transactional(readOnly = true)
    // Retrieves full chronological audit transition log for a specific ticket
    public List<TicketHistoryResponse> getHistoryByTicket(Long ticketId, Authentication authentication) {
        // Queries ticket by identifier throwing HTTP 404 if not found
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        // Checks whether requesting user is authorized to view this ticket's history
        if (!canViewHistory(ticket, authentication)) {
            // Throws HTTP 403 forbidden exception if user lacks viewing permissions
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to view this Ticket history");
        // Closes if condition checking history permissions
        }

        // Queries history records sorted by timestamp ascending and maps to response
        return ticketHistoryRepository.findByTicketIdOrderByChangedAtAscIdAsc(ticketId)
                .stream()
                .map(TicketHistoryResponse::from)
                .toList();
    // Closes getHistoryByTicket method
    }

    /**
     * Checks history viewing permissions.
     */
    // Evaluates whether current user has authorization to view ticket history
    private boolean canViewHistory(Ticket ticket, Authentication authentication) {
        // Grants unconditional history viewing access to system administrators
        if (hasAuthority(authentication, ROLE_ADMIN)) {
            // Returns true granting history viewing access for administrator role
            return true;
        // Closes if condition checking admin role
        }
        // Extracts username email of currently authenticated principal user
        String email = authentication.getName();

        // Checks whether requesting user possesses support technician role authority
        if (hasAuthority(authentication, ROLE_TECHNICIAN)) {
            // Retrieves technician assigned to this ticket for verification
            User technician = ticket.getTechnicianAssigned();
            // Allows access if technician is non-null and assigned to this ticket
            return technician != null && email.equalsIgnoreCase(technician.getEmail());
        // Closes if condition checking technician role
        }
        // Retrieves requester user entity who created this support ticket
        User requester = ticket.getRequester();
        // Allows access if requester is non-null and matches current user's email
        return requester != null && email.equalsIgnoreCase(requester.getEmail());
    // Closes canViewHistory method
    }

    /**
     * Checks user granted authority.
     */
    // Evaluates whether authentication contains the specified authority string
    private boolean hasAuthority(Authentication authentication, String authority) {
        // Iterates through granted authorities associated with authenticated user
        for (GrantedAuthority granted : authentication.getAuthorities()) {
            // Compares granted authority string with specified authority argument
            if (authority.equals(granted.getAuthority())) {
                // Returns true immediately upon finding matching authority
                return true;
            // Closes if condition checking matching authority
            }
        // Closes for loop iterating authorities
        }
        // Returns false when matching security authority is not found
        return false;
    // Closes hasAuthority helper method
    }
// Closes TicketHistoryService class definition
}
