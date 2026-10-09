package com.spring.dto.response;

import com.spring.Ticket.TicketStatus; // ASSUMPTION

import java.time.LocalDateTime;

public record TicketHistoryResponse(
        Long id,
        Long ticketId,
        TicketStatus previousStatus,
        TicketStatus newStatus,
        Long changedById,
        String changedByEmail,
        LocalDateTime changedAt
) {

    public static TicketHistoryResponse from(TicketHistory entry) {
        return new TicketHistoryResponse(
                entry.getId(),
                entry.getTicket().getId(),
                entry.getPreviousStatus(),
                entry.getNewStatus(),
                entry.getChangedBy().getId(),
                entry.getChangedBy().getEmail(), // ASSUMPTION: User#getEmail()
                entry.getChangedAt()
        );
    }
}
