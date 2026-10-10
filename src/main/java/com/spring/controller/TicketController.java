package com.spring.controller;

import com.spring.dto.request.TicketRequest;
import com.spring.dto.response.TicketResponse;
import com.spring.entity.enums.TicketState;
import com.spring.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    // 1. POST /api/tickets -> Crear solicitud
    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@RequestBody TicketRequest request) {
        TicketResponse response = ticketService.createTicket(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 2. GET /api/tickets -> Listar tickets autorizados
    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets(@RequestParam(required = false) TicketState state) {
        return ResponseEntity.ok(ticketService.getAllTickets(state));
    }

    // 3. GET /api/tickets/{id} -> Consultar detalle
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    // 4. PUT /api/tickets/{id} -> Actualizar solicitud permitida
    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable Long id,
            @RequestBody TicketRequest request) {
        return ResponseEntity.ok(ticketService.updateTicket(id, request));
    }

    // 5. PATCH /api/tickets/{id}/assign -> Asignar técnico
    @PatchMapping("/{id}/assign")
    public ResponseEntity<TicketResponse> assignTechnician(
            @PathVariable Long id,
            @RequestParam Long technicianId) {
        return ResponseEntity.ok(ticketService.assignTechnician(id, technicianId));
    }

    // 6. PATCH /api/tickets/{id}/status -> Cambiar estado
    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam TicketState status) {
        return ResponseEntity.ok(ticketService.updateState(id, status));
    }

    // Rutas auxiliares de conveniencia
    @GetMapping("/requester/{requesterId}")
    public ResponseEntity<List<TicketResponse>> getTicketsByRequester(@PathVariable Long requesterId) {
        return ResponseEntity.ok(ticketService.getTicketsByRequester(requesterId));
    }

    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<List<TicketResponse>> getTicketsByTechnician(@PathVariable Long technicianId) {
        return ResponseEntity.ok(ticketService.getTicketsByTechnician(technicianId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}
