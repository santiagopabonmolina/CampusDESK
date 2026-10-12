package com.spring.controller;

import com.spring.dto.response.TicketHistoryResponse;
import com.spring.service.TicketHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "5. Historial y Auditoría de Incidencias", description = "Línea de tiempo inmutable de transiciones de estado de cada ticket")
@RestController
@RequestMapping("/api/tickets/{ticketId}/history")
public class TicketHistoryController {

    private final TicketHistoryService ticketHistoryService;

    public TicketHistoryController(TicketHistoryService ticketHistoryService) {
        this.ticketHistoryService = ticketHistoryService;
    }

    @Operation(summary = "Consultar historial de un ticket", description = "Retorna en orden cronológico todos los cambios de estado sufridos por el ticket, indicando estado previo, nuevo estado, autor del cambio y timestamp.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado a ticket ajeno"),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    @GetMapping
    public List<TicketHistoryResponse> getHistory(@PathVariable Long ticketId,
                                                  Authentication authentication) {
        return ticketHistoryService.getHistoryByTicket(ticketId, authentication);
    }
}
