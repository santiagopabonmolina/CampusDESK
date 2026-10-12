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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
    public List<TicketResponse> getAllTickets(TicketState state, Authentication authentication) {
        if (authentication == null) {
            List<Ticket> tickets = (state != null) ? ticketRepository.findByState(state) : ticketRepository.findAll();
            return tickets.stream().map(TicketResponse::fromEntity).collect(Collectors.toList());
        }

        String email = authentication.getName();
        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
        boolean isTechnician = hasRole(authentication, "ROLE_TECHNICIAN");

        List<Ticket> tickets;
        if (isAdmin) {
            tickets = (state != null) ? ticketRepository.findByState(state) : ticketRepository.findAll();
        } else if (isTechnician) {
            User tech = userRepository.findByEmail(email).orElse(null);
            if (tech == null) return List.of();
            tickets = (state != null)
                    ? ticketRepository.findByTechnicianAssignedIdAndState(tech.getId(), state)
                    : ticketRepository.findByTechnicianAssignedId(tech.getId());
        } else {
            // USER: Solo sus propias solicitudes
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) return List.of();
            tickets = (state != null)
                    ? ticketRepository.findByRequesterIdAndState(user.getId(), state)
                    : ticketRepository.findByRequesterId(user.getId());
        }

        return tickets.stream().map(TicketResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id, Authentication authentication) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        if (authentication != null && !canAccessTicket(ticket, authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado a este ticket");
        }

        return TicketResponse.fromEntity(ticket);
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
        return TicketResponse.fromEntity(ticket);
    }

    @Transactional
    public TicketResponse updateTicket(Long id, TicketRequest request, Authentication authentication) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        if (ticket.getState() != TicketState.OPENED || ticket.getTechnicianAssigned() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden editar tickets ABIERTOS sin asignar");
        }

        if (authentication != null && !hasRole(authentication, "ROLE_ADMIN")) {
            String email = authentication.getName();
            if (ticket.getRequester() == null || !email.equalsIgnoreCase(ticket.getRequester().getEmail())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el solicitante puede modificar su ticket");
            }
        }

        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(request.getCategory());
        ticket.setPriority(request.getPriority());

        return TicketResponse.fromEntity(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse updateTicket(Long id, TicketRequest request) {
        return updateTicket(id, request, null);
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
    public TicketResponse updateState(Long id, TicketState newState, Authentication authentication) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        TicketState previousState = ticket.getState();

        if (previousState == TicketState.CLOSED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ticket ya está CERRADO");
        }

        // CP-10: Resolver un ticket sin pasar por EN_PROCESO -> HTTP 409
        if (newState == TicketState.RESOLVED && previousState != TicketState.IN_PROCESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un ticket solo puede marcarse RESUELTA si previamente estuvo EN_PROCESO");
        }

        // CP-12: Cerrar un ticket RESUELTA como solicitante
        if (newState == TicketState.CLOSED) {
            if (previousState != TicketState.RESOLVED) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Un ticket solo puede cerrarse si previamente está en estado RESUELTA");
            }
            if (authentication != null && !hasRole(authentication, "ROLE_ADMIN")) {
                String email = authentication.getName();
                if (ticket.getRequester() == null || !email.equalsIgnoreCase(ticket.getRequester().getEmail())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el solicitante puede confirmar el cierre de su ticket");
                }
            }
        }

        // CP-09: Un técnico no puede modificar tickets ajenos
        if (newState == TicketState.IN_PROCESS) {
            if (previousState != TicketState.ASSIGNED) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Un ticket solo puede pasar a EN_PROCESO desde ASIGNADA");
            }
            if (authentication != null && !hasRole(authentication, "ROLE_ADMIN")) {
                String email = authentication.getName();
                if (ticket.getTechnicianAssigned() == null || !email.equalsIgnoreCase(ticket.getTechnicianAssigned().getEmail())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el técnico asignado puede iniciar este ticket");
                }
            }
        }

        if (newState == TicketState.RESOLVED) {
            if (authentication != null && !hasRole(authentication, "ROLE_ADMIN")) {
                String email = authentication.getName();
                if (ticket.getTechnicianAssigned() == null || !email.equalsIgnoreCase(ticket.getTechnicianAssigned().getEmail())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el técnico asignado puede resolver este ticket");
                }
            }
            ticket.setResolutionDate(LocalDateTime.now());
        } else if (newState == TicketState.CLOSED) {
            ticket.setCloseDate(LocalDateTime.now());
        }

        ticket.setState(newState);

        User changer = null;
        if (authentication != null) {
            changer = userRepository.findByEmail(authentication.getName()).orElse(null);
        }
        if (changer == null) {
            changer = ticket.getTechnicianAssigned() != null ? ticket.getTechnicianAssigned() : ticket.getRequester();
        }

        ticketHistoryService.recordChange(ticket, previousState, newState, changer);
        return TicketResponse.fromEntity(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse updateState(Long id, TicketState newState) {
        return updateState(id, newState, null);
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

    private boolean canAccessTicket(Ticket ticket, Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN")) return true;
        String email = authentication.getName();
        if (hasRole(authentication, "ROLE_TECHNICIAN")) {
            return ticket.getTechnicianAssigned() != null &&
                    email.equalsIgnoreCase(ticket.getTechnicianAssigned().getEmail());
        }
        return ticket.getRequester() != null &&
                email.equalsIgnoreCase(ticket.getRequester().getEmail());
    }

    private boolean hasRole(Authentication authentication, String role) {
        for (GrantedAuthority ga : authentication.getAuthorities()) {
            if (role.equals(ga.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
