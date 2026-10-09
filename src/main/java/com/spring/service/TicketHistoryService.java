package com.spring.service;

import com.spring.modules.Ticket.Ticket;
import com.spring.modules.Ticket.TicketRepository; // ASSUMPTION: exists with findById
import com.spring.modules.Ticket.TicketStatus;     // ASSUMPTION
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
public class TicketHistoryService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";             // ASSUMPTION: authority names
    private static final String ROLE_TECHNICIAN = "ROLE_TECHNICIAN";

    private final TicketHistoryRepository TicketHistoryRepository;
    private final TicketRepository TicketRepository;

    public TicketHistoryService(TicketHistoryRepository TicketHistoryRepository,
                                TicketRepository TicketRepository) {
        this.TicketHistoryRepository = TicketHistoryRepository;
        this.TicketRepository = TicketRepository;
    }

    /**
     * Records a status change. To be called by the Ticket service in the SAME transaction
     * that updates the Ticket status. MANDATORY fails fast if no transaction is active,
     * guaranteeing that status update and history entry are committed together.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public TicketHistoryResponse recordChange(Ticket Ticket,
                                              TicketStatus previousStatus,
                                              TicketStatus newStatus,
                                              User changedBy) {
        Objects.requireNonNull(Ticket, "Ticket must not be null");
        Objects.requireNonNull(newStatus, "newStatus must not be null");
        Objects.requireNonNull(changedBy, "changedBy must not be null");

        if (previousStatus == newStatus) {
            throw new IllegalArgumentException("Previous and new status must be different");
        }

        // Avoid duplicated entries when the same transition is registered twice in a row.
        // Skipped for new Tickets that do not have an id yet.
        if (Ticket.getId() != null) {
            boolean duplicated = TicketHistoryRepository
                    .findFirstByTicketIdOrderByChangedAtDescIdDesc(Ticket.getId())
                    .map(last -> last.getNewStatus() == newStatus && last.getPreviousStatus() == previousStatus)
                    .orElse(false);
            if (duplicated) {
                throw new IllegalStateException("Duplicated history entry for the same transition");
            }
        }

        TicketHistory saved = TicketHistoryRepository.save(
                new TicketHistory(Ticket, previousStatus, newStatus, changedBy));
        return TicketHistoryResponse.from(saved);
    }

    /** Returns the Ticket history in chronological order, enforcing Ticket visibility rules. */
    @Transactional(readOnly = true)
    public List<TicketHistoryResponse> getHistoryByTicket(Long TicketId, Authentication authentication) {
        Ticket Ticket = TicketRepository.findById(TicketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        if (!canViewHistory(Ticket, authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to view this Ticket history");
        }

        return TicketHistoryRepository.findByTicketIdOrderByChangedAtAscIdAsc(TicketId)
                .stream()
                .map(TicketHistoryResponse::from)
                .toList();
    }

    private boolean canViewHistory(Ticket Ticket, Authentication authentication) {
        if (hasAuthority(authentication, ROLE_ADMIN)) {
            return true;
        }
        String email = authentication.getName(); // ASSUMPTION: JWT subject is the user email

        if (hasAuthority(authentication, ROLE_TECHNICIAN)) {
            User technician = Ticket.getAssignedTechnician(); // ASSUMPTION: field name
            return technician != null && email.equalsIgnoreCase(technician.getEmail());
        }
        User requester = Ticket.getRequester(); // ASSUMPTION: field name
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
