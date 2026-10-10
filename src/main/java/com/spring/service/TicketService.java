package com.spring.service;

import com.spring.dto.request.TicketRequest;
import com.spring.dto.response.TicketResponse;
import com.spring.entity.Ticket;
import com.spring.entity.User;
import com.spring.entity.enums.TicketState;
import com.spring.repository.TicketRepository;
import com.spring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketHistoryService ticketHistoryService;

    @Transactional
    public TicketResponse createTicket(TicketRequest request) {
        User requester = userRepository.findById(request.getRequesterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requester not found"));

        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .priority(request.getPriority())
                .state(TicketState.OPENED)
                .requester(requester)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);
        ticketHistoryService.recordChange(savedTicket, null, TicketState.OPENED, requester);
        return TicketResponse.fromEntity(savedTicket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets(TicketState state) {
        List<Ticket> tickets = (state != null) 
                ? ticketRepository.findByState(state) 
                : ticketRepository.findAll();
        return tickets.stream().map(TicketResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
        return TicketResponse.fromEntity(ticket);
    }

    @Transactional
    public TicketResponse updateTicket(Long id, TicketRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        if (ticket.getState() != TicketState.OPENED || ticket.getTechnicianAssigned() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden editar tickets ABIERTOS sin asignar");
        }

        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(request.getCategory());
        ticket.setPriority(request.getPriority());

        return TicketResponse.fromEntity(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse assignTechnician(Long id, Long technicianId) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Technician not found"));

        TicketState previousState = ticket.getState();
        ticket.setTechnicianAssigned(technician);
        ticket.setState(TicketState.ASSIGNED);

        ticketHistoryService.recordChange(ticket, previousState, TicketState.ASSIGNED, technician);
        return TicketResponse.fromEntity(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse updateState(Long id, TicketState newState) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        TicketState previousState = ticket.getState();
        
        // Simple validation for transition logic
        if (previousState == TicketState.CLOSED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ticket ya está CERRADO");
        }
        
        if (newState == TicketState.RESOLVED) {
            ticket.setResolutionDate(LocalDateTime.now());
        } else if (newState == TicketState.CLOSED) {
            ticket.setCloseDate(LocalDateTime.now());
        }

        ticket.setState(newState);
        // Using requester if technician is null as fallback for who did it (just for simplificy here)
        User changer = ticket.getTechnicianAssigned() != null ? ticket.getTechnicianAssigned() : ticket.getRequester();
        
        ticketHistoryService.recordChange(ticket, previousState, newState, changer);
        return TicketResponse.fromEntity(ticketRepository.save(ticket));
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByRequester(Long requesterId) {
        return ticketRepository.findByRequesterId(requesterId).stream()
                .map(TicketResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByTechnician(Long technicianId) {
        return ticketRepository.findByTechnicianAssignedId(technicianId).stream()
                .map(TicketResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
    }
}
