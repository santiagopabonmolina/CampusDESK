package com.spring.dto.response;

import com.spring.entity.enums.TicketState;
import com.spring.entity.TicketHistory;

import java.time.LocalDateTime;

public record TicketHistoryResponse(
        Long id,
        Long ticketId,
        TicketState previousStatus,
        TicketState newStatus,
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
                entry.getChangedBy().getEmail(),
                entry.getChangedAt()
        );
    }
}
