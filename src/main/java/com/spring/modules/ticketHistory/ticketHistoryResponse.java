package com.spring.modules.ticketHistory;

import com.spring.modules.ticket.ticketStatus; // ASSUMPTION

import java.time.LocalDateTime;

public record ticketHistoryResponse(
        Long id,
        Long ticketId,
        ticketStatus previousStatus,
        ticketStatus newStatus,
        Long changedById,
        String changedByEmail,
        LocalDateTime changedAt
) {

    public static ticketHistoryResponse from(ticketHistory entry) {
        return new ticketHistoryResponse(
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
