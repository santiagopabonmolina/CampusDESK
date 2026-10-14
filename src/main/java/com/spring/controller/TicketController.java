package com.spring.controller;

import com.spring.dto.request.TicketRequest;
import com.spring.dto.response.TicketResponse;
import com.spring.entity.enums.TicketCategory;
import com.spring.entity.enums.TicketPriority;
import com.spring.entity.enums.TicketState;
import com.spring.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    // 1. POST /api/tickets -> Crear solicitud asociada al usuario autenticado
    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @RequestBody TicketRequest request,
            Authentication authentication) {
        TicketResponse response = ticketService.createTicket(request, authentication);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 2. GET /api/tickets -> Listar tickets autorizados con filtros opcionales (estado, prioridad, categoría)
    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets(
            @RequestParam(required = false) TicketState state,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) TicketCategory category,
            Authentication authentication) {
        return ResponseEntity.ok(ticketService.getAllTickets(state, priority, category, authentication));
    }

    // 3. GET /api/tickets/{id} -> Consultar detalle con validación estricta de pertenencia (403/404)
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(ticketService.getTicketById(id, authentication));
    }

    // 4. PUT /api/tickets/{id} -> Actualizar solicitud permitida
    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable Long id,
            @RequestBody TicketRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ticketService.updateTicket(id, request, authentication));
    }

    // 5. PATCH /api/tickets/{id}/assign -> Asignar técnico (solo Admin)
    @PatchMapping("/{id}/assign")
    public ResponseEntity<TicketResponse> assignTechnician(
            @PathVariable Long id,
            @RequestParam Long technicianId,
            Authentication authentication) {
        return ResponseEntity.ok(ticketService.assignTechnician(id, technicianId, authentication));
    }

    // 6. PATCH /api/tickets/{id}/status -> Cambiar estado con validación de roles y transiciones
    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam TicketState status,
            Authentication authentication) {
        return ResponseEntity.ok(ticketService.updateState(id, status, authentication));
    }

    // Rutas auxiliares de conveniencia
    @GetMapping("/requester/{requesterId}")
    public ResponseEntity<List<TicketResponse>> getTicketsByRequester(
            @PathVariable Long requesterId,
            Authentication authentication) {
        return ResponseEntity.ok(ticketService.getTicketsByRequester(requesterId, authentication));
    }

    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<List<TicketResponse>> getTicketsByTechnician(
            @PathVariable Long technicianId,
            Authentication authentication) {
        return ResponseEntity.ok(ticketService.getTicketsByTechnician(technicianId, authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(
            @PathVariable Long id,
            Authentication authentication) {
        ticketService.deleteTicket(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
