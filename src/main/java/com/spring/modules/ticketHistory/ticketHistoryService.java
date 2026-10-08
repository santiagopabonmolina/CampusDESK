package com.spring.modules.ticketHistory;

import com.spring.modules.ticket.ticket;
import com.spring.modules.ticket.ticketRepository; // ASSUMPTION: exists with findById
import com.spring.modules.ticket.ticketStatus;     // ASSUMPTION
import com.spring.modules.user.User;               // ASSUMPTION
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
public class ticketHistoryService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";             // ASSUMPTION: authority names
    private static final String ROLE_TECHNICIAN = "ROLE_TECHNICIAN";

    private final ticketHistoryRepository ticketHistoryRepository;
    private final ticketRepository ticketRepository;

    public ticketHistoryService(ticketHistoryRepository ticketHistoryRepository,
                                ticketRepository ticketRepository) {
        this.ticketHistoryRepository = ticketHistoryRepository;
        this.ticketRepository = ticketRepository;
    }

    /**
     * Records a status change. To be called by the ticket service in the SAME transaction
     * that updates the ticket status. MANDATORY fails fast if no transaction is active,
     * guaranteeing that status update and history entry are committed together.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public ticketHistoryResponse recordChange(ticket ticket,
                                              ticketStatus previousStatus,
                                              ticketStatus newStatus,
                                              User changedBy) {
        Objects.requireNonNull(ticket, "ticket must not be null");
        Objects.requireNonNull(newStatus, "newStatus must not be null");
        Objects.requireNonNull(changedBy, "changedBy must not be null");

        if (previousStatus == newStatus) {
            throw new IllegalArgumentException("Previous and new status must be different");
        }

        // Avoid duplicated entries when the same transition is registered twice in a row.
        // Skipped for new tickets that do not have an id yet.
        if (ticket.getId() != null) {
            boolean duplicated = ticketHistoryRepository
                    .findFirstByTicketIdOrderByChangedAtDescIdDesc(ticket.getId())
                    .map(last -> last.getNewStatus() == newStatus && last.getPreviousStatus() == previousStatus)
                    .orElse(false);
            if (duplicated) {
                throw new IllegalStateException("Duplicated history entry for the same transition");
            }
        }

        ticketHistory saved = ticketHistoryRepository.save(
                new ticketHistory(ticket, previousStatus, newStatus, changedBy));
        return ticketHistoryResponse.from(saved);
    }

    /** Returns the ticket history in chronological order, enforcing ticket visibility rules. */
    @Transactional(readOnly = true)
    public List<ticketHistoryResponse> getHistoryByTicket(Long ticketId, Authentication authentication) {
        ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        if (!canViewHistory(ticket, authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to view this ticket history");
        }

        return ticketHistoryRepository.findByTicketIdOrderByChangedAtAscIdAsc(ticketId)
                .stream()
                .map(ticketHistoryResponse::from)
                .toList();
    }

    private boolean canViewHistory(ticket ticket, Authentication authentication) {
        if (hasAuthority(authentication, ROLE_ADMIN)) {
            return true;
        }
        String email = authentication.getName(); // ASSUMPTION: JWT subject is the user email

        if (hasAuthority(authentication, ROLE_TECHNICIAN)) {
            User technician = ticket.getAssignedTechnician(); // ASSUMPTION: field name
            return technician != null && email.equalsIgnoreCase(technician.getEmail());
        }
        User requester = ticket.getRequester(); // ASSUMPTION: field name
        return requester != null && email.equalsIgnoreCase(requester.getEmail());
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        for (GrantedAuthority granted : authentication.getAuthorities()) {
            if (authority.equals(granted.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
