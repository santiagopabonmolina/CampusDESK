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

    // 1. Crear nuevo ticket (POST /api/tickets)
    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@RequestBody TicketRequest request) {
        TicketResponse response = ticketService.createTicket(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 2. Obtener todos los tickets, con filtro opcional por estado (GET /api/tickets?state=OPENED)
    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets(@RequestParam(required = false) TicketState state) {
        return ResponseEntity.ok(ticketService.getAllTickets(state));
    }

    // 3. Obtener ticket por ID (GET /api/tickets/{id})
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    // 4. Listar tickets creados por un solicitante (GET /api/tickets/requester/{requesterId})
    @GetMapping("/requester/{requesterId}")
    public ResponseEntity<List<TicketResponse>> getTicketsByRequester(@PathVariable Long requesterId) {
        return ResponseEntity.ok(ticketService.getTicketsByRequester(requesterId));
    }

    // 5. Listar tickets asignados a un técnico (GET /api/tickets/technician/{technicianId})
    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<List<TicketResponse>> getTicketsByTechnician(@PathVariable Long technicianId) {
        return ResponseEntity.ok(ticketService.getTicketsByTechnician(technicianId));
    }

    // 6. Asignar técnico (PATCH /api/tickets/{id}/assign/{technicianId})
    @PatchMapping("/{id}/assign/{technicianId}")
    public ResponseEntity<TicketResponse> assignTechnician(
            @PathVariable Long id,
            @PathVariable Long technicianId) {
        return ResponseEntity.ok(ticketService.assignTechnician(id, technicianId));
    }

    // 7. Cambiar estado (PATCH /api/tickets/{id}/state?newState=RESOLVED)
    @PatchMapping("/{id}/state")
    public ResponseEntity<TicketResponse> updateTicketState(
            @PathVariable Long id,
            @RequestParam TicketState newState) {
        return ResponseEntity.ok(ticketService.updateState(id, newState));
    }

    // 8. Actualizar información general (PUT /api/tickets/{id})
    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable Long id,
            @RequestBody TicketRequest request) {
        return ResponseEntity.ok(ticketService.updateTicket(id, request));
    }

    // 9. Eliminar ticket (DELETE /api/tickets/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}