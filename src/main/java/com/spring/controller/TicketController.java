package com.spring.controller;

import com.spring.dto.request.CommentRequest;
import com.spring.dto.request.TicketRequest;
import com.spring.dto.response.CommentResponse;
import com.spring.dto.response.TicketResponse;
import com.spring.entity.enums.TicketCategory;
import com.spring.entity.enums.TicketPriority;
import com.spring.entity.enums.TicketState;
import com.spring.service.CommentService;
import com.spring.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "2. Incidencias Tecnológicas (Tickets)", description = "Control completo del ciclo de vida de tickets con control de acceso basado en roles (RBAC)")
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final CommentService commentService;

    @Operation(summary = "Crear nuevo ticket", description = "Registra una nueva incidencia con estado inicial ABIERTA (OPENED) y prioridad/categoría especificadas.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ticket creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody TicketRequest request) {
        TicketResponse response = ticketService.createTicket(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Listar tickets autorizados", description = "Lista tickets con aislamiento de roles: ADMIN ve todos, TECHNICIAN ve sus asignados, USER ve solo sus solicitudes creadas. Permite filtrar por estado/status, prioridad y categoría.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de incidencias devuelto con éxito"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets(
            @Parameter(description = "Filtrar por estado: OPENED, ASSIGNED, IN_PROCESS, RESOLVED, CLOSED")
            @RequestParam(name = "state", required = false) TicketState state,
            @Parameter(description = "Alias para filtrar por estado: OPENED, ASSIGNED, IN_PROCESS, RESOLVED, CLOSED")
            @RequestParam(name = "status", required = false) TicketState status,
            @Parameter(description = "Filtrar por prioridad: LOW, MEDIUM, HIGH, CRITIC")
            @RequestParam(required = false) TicketPriority priority,
            @Parameter(description = "Filtrar por categoría: HARDWARE, SOFTWARE, NETWORK, ACCESS, OTHER")
            @RequestParam(required = false) TicketCategory category,
            Authentication authentication) {
        TicketState filterState = (state != null) ? state : status;
        return ResponseEntity.ok(ticketService.getAllTickets(filterState, priority, category, authentication));
    }

    @Operation(summary = "Consultar detalle de un ticket", description = "Obtiene información completa de la incidencia. Valida que el usuario tenga permisos sobre el recurso.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle del ticket obtenido"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado a tickets ajenos"),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(ticketService.getTicketById(id, authentication));
    }

    @Operation(summary = "Modificar solicitud abierta", description = "Permite actualizar título, descripción, prioridad o categoría. Solo permitido mientras el ticket esté en estado ABIERTA y sin asignar.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket actualizado"),
            @ApiResponse(responseCode = "403", description = "No es el propietario del ticket"),
            @ApiResponse(responseCode = "409", description = "El ticket ya no está en estado ABIERTA o ya fue asignado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable Long id,
            @Valid @RequestBody TicketRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ticketService.updateTicket(id, request, authentication));
    }

    @Operation(summary = "Asignar o reasignar técnico", description = "Exclusivo de ADMIN. Asigna un técnico y cambia el estado a ASIGNADA.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Técnico asignado exitosamente"),
            @ApiResponse(responseCode = "400", description = "El usuario seleccionado no tiene rol TECHNICIAN"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN")
    })
    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> assignTechnician(
            @PathVariable Long id,
            @RequestParam Long technicianId,
            Authentication authentication) {
        return ResponseEntity.ok(ticketService.assignTechnician(id, technicianId, authentication));
    }

    @Operation(summary = "Transición de estado del ciclo de vida", description = "Valida reglas de negocio estrictas: ABIERTA ➔ ASIGNADA ➔ EN_PROCESO ➔ RESUELTA ➔ CERRADA.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado con éxito"),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes para esta transición"),
            @ApiResponse(responseCode = "409", description = "Transición inválida o ticket ya cerrado")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponse> changeStatus(
            @PathVariable Long id,
            @RequestParam(name = "status", required = false) TicketState status,
            @RequestParam(name = "state", required = false) TicketState state,
            Authentication authentication) {
        TicketState targetState = (status != null) ? status : state;
        return ResponseEntity.ok(ticketService.updateState(id, targetState, authentication));
    }

    @Operation(summary = "Eliminar ticket", description = "Exclusivo de ADMIN. Elimina una incidencia y sus registros asociados.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ticket eliminado"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTicket(
            @PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Consultar comentarios de un ticket", description = "Retorna la conversación cronológica asociada a la incidencia.")
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentsByTicketId(
            @PathVariable Long id) {
        return ResponseEntity.ok(commentService.getCommentsByTicketId(id));
    }

    @Operation(summary = "Agregar comentario a ticket", description = "Registra una nota o respuesta. Rechaza comentarios si el ticket está CERRADO.")
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {
        request.setTicketId(id);
        return new ResponseEntity<>(commentService.createComment(request, authentication), HttpStatus.CREATED);
    }
}
