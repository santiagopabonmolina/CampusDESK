package com.spring.controller;

import com.spring.dto.request.CommentRequest;
import com.spring.dto.request.TicketRequest;
import com.spring.dto.response.CommentResponse;
import com.spring.dto.response.TicketResponse;
import com.spring.entity.enums.TicketState;
import com.spring.service.CommentService;
import com.spring.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "2. Gestión de Tickets e Incidencias", description = "Operaciones del ciclo de vida de incidencias (ABIERTA ➔ ASIGNADA ➔ EN_PROCESO ➔ RESUELTA ➔ CERRADA)")
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final CommentService commentService;

    @Operation(summary = "Crear nueva solicitud de soporte", description = "Registra una incidencia. Inicia automáticamente en estado ABIERTA y sin técnico.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ticket creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos obligatorios faltantes o inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@RequestBody TicketRequest request) {
        TicketResponse response = ticketService.createTicket(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Listar tickets autorizados", description = "Lista tickets con aislamiento de roles: ADMIN ve todos, TECHNICIAN ve sus asignados, USER ve solo sus solicitudes creadas. Permite filtrar por estado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de incidencias devuelto con éxito"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets(
            @Parameter(description = "Filtrar por estado: OPENED, ASSIGNED, IN_PROCESS, RESOLVED, CLOSED")
            @RequestParam(required = false) TicketState state,
            Authentication authentication) {
        return ResponseEntity.ok(ticketService.getAllTickets(state, authentication));
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
            @RequestBody TicketRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ticketService.updateTicket(id, request, authentication));
    }

    @Operation(summary = "Asignar o reasignar técnico (Solo ADMIN)", description = "Asigna un técnico disponible a la incidencia. El estado transiciona automáticamente a ASIGNADA.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Técnico asignado exitosamente"),
            @ApiResponse(responseCode = "403", description = "Permiso denegado (requiere rol ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Ticket o técnico no encontrado")
    })
    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> assignTechnician(
            @PathVariable Long id,
            @RequestParam Long technicianId) {
        return ResponseEntity.ok(ticketService.assignTechnician(id, technicianId));
    }

    @Operation(summary = "Cambiar estado de la incidencia", description = "Ejecuta transiciones del ciclo de vida respetando reglas estrictas: RESUELTA requiere EN_PROCESO; CERRADA solo por el solicitante en ticket RESUELTO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado cambiado exitosamente"),
            @ApiResponse(responseCode = "403", description = "Operación no permitida para el rol del usuario"),
            @ApiResponse(responseCode = "409", description = "Conflicto con las reglas de transición del ciclo de vida")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponse> updateStatus(
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado: OPENED, ASSIGNED, IN_PROCESS, RESOLVED, CLOSED")
            @RequestParam TicketState status,
            Authentication authentication) {
        return ResponseEntity.ok(ticketService.updateState(id, status, authentication));
    }

    @Operation(summary = "Obtener comentarios de un ticket", description = "Consulta los comentarios asociados ordenados cronológicamente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de comentarios"),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getCommentsByTicketId(id));
    }

    @Operation(summary = "Registrar comentario en un ticket", description = "Agrega un comentario a la incidencia. Se rechaza si el ticket ya está en estado CERRADA.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comentario registrado"),
            @ApiResponse(responseCode = "409", description = "No se permite comentar en tickets cerrados")
    })
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long id,
            @RequestBody CommentRequest request) {
        request.setTicketId(id);
        return new ResponseEntity<>(commentService.createComment(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Consultar tickets por solicitante", description = "Ruta auxiliar para listar tickets creados por un usuario específico.")
    @GetMapping("/requester/{requesterId}")
    public ResponseEntity<List<TicketResponse>> getTicketsByRequester(@PathVariable Long requesterId) {
        return ResponseEntity.ok(ticketService.getTicketsByRequester(requesterId));
    }

    @Operation(summary = "Consultar tickets por técnico", description = "Ruta auxiliar para listar tickets asignados a un técnico específico.")
    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<List<TicketResponse>> getTicketsByTechnician(@PathVariable Long technicianId) {
        return ResponseEntity.ok(ticketService.getTicketsByTechnician(technicianId));
    }

    @Operation(summary = "Eliminar ticket (Solo ADMIN)", description = "Elimina permanentemente una incidencia.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}
