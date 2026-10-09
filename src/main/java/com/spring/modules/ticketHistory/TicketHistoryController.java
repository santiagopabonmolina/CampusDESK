package com.spring.modules.ticketHistory;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only endpoint: history records are created by the service layer only.
 * No POST, PUT, PATCH or DELETE is exposed on purpose.
 */
@RestController
@RequestMapping("/api/tickets/{ticketId}/history")
public class TicketHistoryController {

    private final TicketHistoryService TicketHistoryService;

    public TicketHistoryController(TicketHistoryService TicketHistoryService) {
        this.TicketHistoryService = TicketHistoryService;
    }

    @GetMapping
    public List<TicketHistoryResponse> getHistory(@PathVariable Long TicketId,
                                                  Authentication authentication) {
        return TicketHistoryService.getHistoryByTicket(ticketId, authentication);
    }
}
