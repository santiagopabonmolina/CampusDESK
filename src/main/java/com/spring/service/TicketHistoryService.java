package com.spring.service;

import com.spring.entity.Ticket;
import com.spring.repository.TicketRepository;
import com.spring.entity.enums.TicketState;
import com.spring.entity.User;
import com.spring.entity.TicketHistory;
import com.spring.repository.TicketHistoryRepository;
import com.spring.dto.response.TicketHistoryResponse;
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

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_TECHNICIAN = "ROLE_TECHNICIAN";

    private final TicketHistoryRepository ticketHistoryRepository;
    private final TicketRepository ticketRepository;

    public TicketHistoryService(TicketHistoryRepository ticketHistoryRepository,
                                TicketRepository ticketRepository) {
        this.ticketHistoryRepository = ticketHistoryRepository;
        this.ticketRepository = ticketRepository;
    }

    /**
     * Records a status change. To be called by the Ticket service in the SAME transaction
     * that updates the Ticket status. MANDATORY fails fast if no transaction is active,
     * guaranteeing that status update and history entry are committed together.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public TicketHistoryResponse recordChange(Ticket ticket,
                                              TicketState previousStatus,
                                              TicketState newStatus,
                                              User changedBy) {
        Objects.requireNonNull(ticket, "ticket must not be null");
        Objects.requireNonNull(newStatus, "newStatus must not be null");
        Objects.requireNonNull(changedBy, "changedBy must not be null");

        if (previousStatus == newStatus) {
            throw new IllegalArgumentException("Previous and new status must be different");
        }

        // Avoid duplicated entries when the same transition is registered twice in a row.
        // Skipped for new Tickets that do not have an id yet.
        if (ticket.getId() != null) {
            boolean duplicated = ticketHistoryRepository
                    .findFirstByTicketIdOrderByChangedAtDescIdDesc(ticket.getId())
                    .map(last -> last.getNewStatus() == newStatus && last.getPreviousStatus() == previousStatus)
                    .orElse(false);
            if (duplicated) {
                throw new IllegalStateException("Duplicated history entry for the same transition");
            }
        }

        TicketHistory saved = ticketHistoryRepository.save(
                new TicketHistory(ticket, previousStatus, newStatus, changedBy));
        return TicketHistoryResponse.from(saved);
    }

    /** Returns the Ticket history in chronological order, enforcing Ticket visibility rules. */
    @Transactional(readOnly = true)
    public List<TicketHistoryResponse> getHistoryByTicket(Long ticketId, Authentication authentication) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        if (!canViewHistory(ticket, authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to view this Ticket history");
        }

        return ticketHistoryRepository.findByTicketIdOrderByChangedAtAscIdAsc(ticketId)
                .stream()
                .map(TicketHistoryResponse::from)
                .toList();
    }

    private boolean canViewHistory(Ticket ticket, Authentication authentication) {
        if (hasAuthority(authentication, ROLE_ADMIN)) {
            return true;
        }
        String email = authentication.getName();

        if (hasAuthority(authentication, ROLE_TECHNICIAN)) {
            User technician = ticket.getTechnicianAssigned();
            return technician != null && email.equalsIgnoreCase(technician.getEmail());
        }
        User requester = ticket.getRequester();
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
