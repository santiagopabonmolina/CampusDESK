// Defines the package namespace containing Spring MVC REST controllers
package com.spring.controller;

// Imports TicketHistoryResponse record DTO carrying history timeline data
import com.spring.dto.response.TicketHistoryResponse;
// Imports TicketHistoryService handling business queries for audit history
import com.spring.service.TicketHistoryService;
// Imports Swagger Operation annotation describing REST endpoint operation details
import io.swagger.v3.oas.annotations.Operation;
// Imports Swagger ApiResponse annotation detailing individual HTTP responses
import io.swagger.v3.oas.annotations.responses.ApiResponse;
// Imports Swagger ApiResponses annotation grouping multiple API response descriptions
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// Imports Swagger Tag annotation categorizing controllers in OpenAPI documentation
import io.swagger.v3.oas.annotations.tags.Tag;
// Imports Spring Security Authentication interface representing authenticated principal
import org.springframework.security.core.Authentication;
// Imports GetMapping annotation mapping HTTP GET requests onto handler methods
import org.springframework.web.bind.annotation.GetMapping;
// Imports PathVariable annotation binding URL template variables to method parameters
import org.springframework.web.bind.annotation.PathVariable;
// Imports RequestMapping annotation mapping web requests onto controller classes
import org.springframework.web.bind.annotation.RequestMapping;
// Imports RestController annotation declaring this class as RESTful controller bean
import org.springframework.web.bind.annotation.RestController;

// Imports Java utility List collection interface for returning sequential lists
import java.util.List;

/**
 * Read-only endpoint: history records are created by the service layer only.
 * No POST, PUT, PATCH or DELETE is exposed on purpose.
 */
// Categorizes ticket history audit endpoints under section five in Swagger
@Tag(name = "5. Historial y Auditoría de Incidencias", description = "Línea de tiempo inmutable de transiciones de estado de cada ticket")
// Marks this class as REST controller serializing responses directly to JSON
@RestController
// Maps base URL route for ticket audit timeline history to sub-path
@RequestMapping("/api/tickets/{ticketId}/history")
// Declares TicketHistoryController exposing read-only RESTful endpoint for ticket audit history
public class TicketHistoryController {

    // Declares final business service reference for managing ticket history records
    private final TicketHistoryService ticketHistoryService;

    /**
     * Initializes ticket history controller.
     */
    // Constructor injecting TicketHistoryService dependency into controller bean
    public TicketHistoryController(TicketHistoryService ticketHistoryService) {
        // Assigns injected TicketHistoryService instance to final controller field
        this.ticketHistoryService = ticketHistoryService;
    // Closes constructor definition
    }

    /**
     * Retrieves ticket audit history.
     */
    // Documents chronological ticket history query endpoint behavior in Swagger UI
    @Operation(summary = "Consultar historial de un ticket", description = "Retorna en orden cronológico todos los cambios de estado sufridos por el ticket, indicando estado previo, nuevo estado, autor del cambio y timestamp.")
    // Configures OpenAPI responses for successful retrieval, forbidden, and not found
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado a ticket ajeno"),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    // Maps HTTP GET requests on history sub-path to this handler method
    @GetMapping
    // Retrieves chronological audit history validating requesting user authorization
    public List<TicketHistoryResponse> getHistory(@PathVariable Long ticketId,
                                                  Authentication authentication) {
        // Queries history records from service layer and returns response list
        return ticketHistoryService.getHistoryByTicket(ticketId, authentication);
    // Closes getHistory handler method
    }
// Closes TicketHistoryController class definition
}
