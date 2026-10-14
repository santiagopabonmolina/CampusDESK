package com.spring.service;

import com.spring.dto.request.TicketRequest;
import com.spring.dto.response.TicketResponse;
import com.spring.entity.Ticket;
import com.spring.entity.User;
import com.spring.entity.enums.TicketCategory;
import com.spring.entity.enums.TicketPriority;
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

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }

    private boolean hasRole(Authentication authentication, String roleName) {
        if (authentication == null || authentication.getAuthorities() == null) return false;
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (roleName.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public TicketResponse createTicket(TicketRequest request, Authentication authentication) {
        User currentUser = getAuthenticatedUser(authentication);
        User requester;

        if (hasRole(authentication, "ROLE_ADMIN") && request.getRequesterId() != null) {
            requester = userRepository.findById(request.getRequesterId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requester not found"));
        } else {
            requester = currentUser;
        }

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
    public List<TicketResponse> getAllTickets(TicketState state,
                                             TicketPriority priority,
                                             TicketCategory category,
                                             Authentication authentication) {
        User currentUser = getAuthenticatedUser(authentication);
        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
        boolean isTechnician = hasRole(authentication, "ROLE_TECHNICIAN");

        Long requesterId = null;
        Long technicianId = null;

        if (isAdmin) {
            requesterId = null;
            technicianId = null;
        } else if (isTechnician) {
            technicianId = currentUser.getId();
        } else {
            // Standard USER: strictly sees ONLY their own tickets
            requesterId = currentUser.getId();
        }

        List<Ticket> tickets = ticketRepository.findFilteredTickets(
                requesterId, technicianId, state, priority, category);

        return tickets.stream().map(TicketResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets(TicketState state) {
        List<Ticket> tickets = (state != null)
                ? ticketRepository.findByState(state)
                : ticketRepository.findAll();
        return tickets.stream().map(TicketResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id, Authentication authentication) {
        User currentUser = getAuthenticatedUser(authentication);
        Ticket ticket = ticketRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket no encontrado"));

        if (!canViewTicket(ticket, currentUser, authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver este ticket");
        }

        return TicketResponse.fromEntity(ticket);
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket no encontrado"));
        return TicketResponse.fromEntity(ticket);
    }

    private boolean canViewTicket(Ticket ticket, User currentUser, Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return true;
        }
        if (hasRole(authentication, "ROLE_TECHNICIAN")) {
            return ticket.getTechnicianAssigned() != null &&
                    ticket.getTechnicianAssigned().getId().equals(currentUser.getId());
        }
        // ROLE_USER: strictly requester
        return ticket.getRequester() != null &&
                ticket.getRequester().getId().equals(currentUser.getId());
    }

    @Transactional
    public TicketResponse updateTicket(Long id, TicketRequest request, Authentication authentication) {
        User currentUser = getAuthenticatedUser(authentication);
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket no encontrado"));

        if (!hasRole(authentication, "ROLE_ADMIN")) {
            if (ticket.getRequester() == null || !ticket.getRequester().getId().equals(currentUser.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para modificar este ticket");
            }
        }

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
    public TicketResponse assignTechnician(Long id, Long technicianId, Authentication authentication) {
        if (!hasRole(authentication, "ROLE_ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo administradores pueden asignar técnicos");
        }
        return assignTechnician(id, technicianId);
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
        User currentUser = getAuthenticatedUser(authentication);
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket no encontrado"));

        TicketState previousState = ticket.getState();
        if (previousState == TicketState.CLOSED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ticket ya está CERRADO");
        }

        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
        boolean isOwner = ticket.getRequester() != null && ticket.getRequester().getId().equals(currentUser.getId());
        boolean isAssignedTech = ticket.getTechnicianAssigned() != null && ticket.getTechnicianAssigned().getId().equals(currentUser.getId());

        if (!isAdmin) {
            if (newState == TicketState.CLOSED) {
                if (!isOwner) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el solicitante puede confirmar el cierre");
                }
                if (previousState != TicketState.RESOLVED) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se puede cerrar un ticket en estado RESUELTO");
                }
            } else if (newState == TicketState.IN_PROCESS || newState == TicketState.RESOLVED) {
                if (!isAssignedTech) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el técnico asignado puede cambiar este estado");
                }
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cambio de estado no permitido");
            }
        }

        if (newState == TicketState.RESOLVED) {
            ticket.setResolutionDate(LocalDateTime.now());
        } else if (newState == TicketState.CLOSED) {
            ticket.setCloseDate(LocalDateTime.now());
        }

        ticket.setState(newState);
        ticketHistoryService.recordChange(ticket, previousState, newState, currentUser);
        return TicketResponse.fromEntity(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse updateState(Long id, TicketState newState) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        TicketState previousState = ticket.getState();
        if (previousState == TicketState.CLOSED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ticket ya está CERRADO");
        }

        if (newState == TicketState.RESOLVED) {
            ticket.setResolutionDate(LocalDateTime.now());
        } else if (newState == TicketState.CLOSED) {
            ticket.setCloseDate(LocalDateTime.now());
        }

        ticket.setState(newState);
        User changer = ticket.getTechnicianAssigned() != null ? ticket.getTechnicianAssigned() : ticket.getRequester();
        ticketHistoryService.recordChange(ticket, previousState, newState, changer);
        return TicketResponse.fromEntity(ticketRepository.save(ticket));
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByRequester(Long requesterId, Authentication authentication) {
        User currentUser = getAuthenticatedUser(authentication);
        if (!hasRole(authentication, "ROLE_ADMIN") && !currentUser.getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver estos tickets");
        }
        return getTicketsByRequester(requesterId);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByRequester(Long requesterId) {
        return ticketRepository.findByRequesterId(requesterId).stream()
                .map(TicketResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByTechnician(Long technicianId, Authentication authentication) {
        User currentUser = getAuthenticatedUser(authentication);
        if (!hasRole(authentication, "ROLE_ADMIN") && !currentUser.getId().equals(technicianId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver estos tickets");
        }
        return getTicketsByTechnician(technicianId);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByTechnician(Long technicianId) {
        return ticketRepository.findByTechnicianAssignedId(technicianId).stream()
                .map(TicketResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public void deleteTicket(Long id, Authentication authentication) {
        if (!hasRole(authentication, "ROLE_ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo los administradores pueden eliminar tickets");
        }
        deleteTicket(id);
    }

    @Transactional
    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
    }
}
