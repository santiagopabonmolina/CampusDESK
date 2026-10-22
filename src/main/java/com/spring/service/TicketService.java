// Defines the package namespace containing business service layer classes
package com.spring.service;

// Imports TicketRequest DTO carrying ticket creation and update payload data
import com.spring.dto.request.TicketRequest;
// Imports TicketResponse DTO transferring ticket entity information to presentation
import com.spring.dto.response.TicketResponse;
// Imports Ticket JPA entity representing support tickets in database
import com.spring.entity.Tag;
import com.spring.entity.Ticket;
// Imports User JPA entity representing user account records
import com.spring.entity.User;
// Imports RoleName enum representing supported system authorization roles
import com.spring.entity.enums.RoleName;
// Imports TicketCategory enum representing incident classification categories
import com.spring.entity.enums.TicketCategory;
// Imports TicketPriority enum representing incident urgency priority levels
import com.spring.entity.enums.TicketPriority;
// Imports TicketState enum representing ticket workflow progression stages
import com.spring.entity.enums.TicketState;
// Imports TicketRepository interface providing ticket database query operations
import com.spring.repository.TagRepository;
import com.spring.repository.TicketRepository;
// Imports UserRepository interface providing user lookup and query operations
import com.spring.repository.UserRepository;
// Imports Lombok annotation generating constructor for final dependency fields
import lombok.RequiredArgsConstructor;
// Imports HttpStatus enum representing standard HTTP status response codes
import org.springframework.http.HttpStatus;
// Imports Spring Security Authentication interface representing authenticated principal
import org.springframework.security.core.Authentication;
// Imports GrantedAuthority interface representing security authorities and roles
import org.springframework.security.core.GrantedAuthority;
// Imports Spring Service annotation declaring this class as business service component
import org.springframework.stereotype.Service;
// Imports Spring Transactional annotation for managing transactional database boundaries
import org.springframework.transaction.annotation.Transactional;
// Imports ResponseStatusException for throwing HTTP-aware exceptions from services
import org.springframework.web.server.ResponseStatusException;

// Imports Java time class representing current date and local time
import java.time.LocalDateTime;
// Imports Java utility List collection interface for handling sequential lists
import java.util.List;
// Imports Collectors utility class for accumulating stream elements into collections
import java.util.stream.Collectors;

// Marks this class as Spring service component for ticket management operations
@Service
// Generates constructor injecting required final service and repository dependencies
@RequiredArgsConstructor
// Declares TicketService handling comprehensive business logic and workflows for tickets
public class TicketService {

    // Declares final repository reference for executing ticket database persistence operations
    private final TicketRepository ticketRepository;
    // Declares final repository reference for looking up user entities by ID or email
    private final UserRepository userRepository;
    // Declares final service reference for recording immutable audit state transition logs
    private final TicketHistoryService ticketHistoryService;

    /**
     * Creates and persists ticket.
     */
    // Manages database transaction boundary ensuring atomic ticket creation and history recording
    @Transactional
    // Creates a new support ticket validating requester existence and setting initial state
    public TicketResponse createTicket(TicketRequest request) {
        // Retrieves requester user entity by ID or throws HTTP 404 if not found
        User requester = userRepository.findById(request.getRequesterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requester not found"));

        // Builds Ticket entity populating attributes and initializing state to OPENED
        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .priority(request.getPriority())
                .state(TicketState.OPENED)
                .requester(requester)
                .build();

        // Persists the new Ticket entity to PostgreSQL database table
        Ticket savedTicket = ticketRepository.save(ticket);
        // Records the initial ticket creation in the immutable ticket history audit log
        ticketHistoryService.recordChange(savedTicket, null, TicketState.OPENED, requester);
        // Converts saved entity into outbound response DTO and returns it
        return TicketResponse.fromEntity(savedTicket);
    // Closes createTicket method
    }

    /**
     * Retrieves filtered ticket list.
     */
    // Configures read-only transaction optimizing performance for querying ticket lists
    @Transactional(readOnly = true)
    // Retrieves tickets filtered by state, priority, category, and authenticated user role
    public List<TicketResponse> getAllTickets(TicketState state, TicketPriority priority,
                                              TicketCategory category, Authentication authentication) {
        // Declares list variable to hold tickets matching authorization context
        List<Ticket> tickets;

        // Evaluates whether request is unauthenticated returning all tickets
        if (authentication == null) {
            // Queries all tickets across the system for unauthenticated requests
            tickets = ticketRepository.findAll();
        // Handles role-based ticket filtering for authenticated system users
        } else {
            // Extracts username email of authenticated user from security principal
            String email = authentication.getName();
            // Evaluates whether current user possesses administrator authorization role
            boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
            // Evaluates whether current user possesses technician authorization role
            boolean isTechnician = hasRole(authentication, "ROLE_TECHNICIAN");

            // Grants complete system-wide ticket visibility to system administrators
            if (isAdmin) {
                // Queries all tickets across the system for administrator role
                tickets = ticketRepository.findAll();
            // Restricts ticket visibility to assigned tickets for technicians
            } else if (isTechnician) {
                // Finds technician user entity by institutional login email
                User tech = userRepository.findByEmail(email).orElse(null);
                // Returns empty list if technician account is not found
                if (tech == null) return List.of();
                // Queries tickets assigned to this technician by their user ID
                tickets = ticketRepository.findByTechnicianAssignedId(tech.getId());
            // Restricts ticket visibility to owned tickets for standard requesters
            } else {
                // Finds requester user entity by institutional login email
                User user = userRepository.findByEmail(email).orElse(null);
                // Returns empty list if requester account is not found
                if (user == null) return List.of();
                // Queries tickets requested by this user by their user ID
                tickets = ticketRepository.findByRequesterId(user.getId());
            // Closes else block for requester ticket queries
            }
        // Closes else block for authenticated ticket queries
        }

        // Filters tickets by lifecycle state if state query parameter is supplied
        if (state != null) {
            // Filters collection retaining only tickets matching specified state parameter
            tickets = tickets.stream().filter(t -> t.getState() == state).collect(Collectors.toList());
        // Closes if condition checking state parameter
        }
        // Filters tickets by priority if priority query parameter is supplied
        if (priority != null) {
            // Filters collection retaining only tickets matching specified priority parameter
            tickets = tickets.stream().filter(t -> t.getPriority() == priority).collect(Collectors.toList());
        // Closes if condition checking priority parameter
        }
        // Filters tickets by category if category query parameter is supplied
        if (category != null) {
            // Filters collection retaining only tickets matching specified category parameter
            tickets = tickets.stream().filter(t -> t.getCategory() == category).collect(Collectors.toList());
        // Closes if condition checking category parameter
        }

        // Maps filtered ticket entities to outbound TicketResponse DTOs and returns list
        return tickets.stream().map(TicketResponse::fromEntity).collect(Collectors.toList());
    // Closes getAllTickets method
    }

    /**
     * Retrieves ticket by id.
     */
    // Configures read-only transaction optimizing performance for querying ticket by ID
    @Transactional(readOnly = true)
    // Retrieves ticket by ID verifying access authorization for requesting user
    public TicketResponse getTicketById(Long id, Authentication authentication) {
        // Queries ticket by ID throwing HTTP 404 if ticket does not exist
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        // Verifies whether authenticated user is authorized to access this ticket
        if (authentication != null && !canAccessTicket(ticket, authentication)) {
            // Throws HTTP 403 forbidden exception if user is not authorized to view ticket
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado a este ticket");
        // Closes if condition checking access authorization
        }

        // Converts verified ticket entity into outbound response DTO and returns it
        return TicketResponse.fromEntity(ticket);
    // Closes getTicketById method with authentication
    }

    /**
     * Finds ticket by identifier.
     */
    // Configures read-only transaction optimizing performance for finding ticket by ID
    @Transactional(readOnly = true)
    // Overloaded method retrieving ticket by ID without requiring authentication check
    public TicketResponse getTicketById(Long id) {
        // Queries ticket by ID throwing HTTP 404 if ticket does not exist
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
        // Converts ticket entity into outbound response DTO and returns it
        return TicketResponse.fromEntity(ticket);
    // Closes getTicketById overloaded method
    }

    /**
     * Updates open ticket entity.
     */
    // Manages database transaction boundary ensuring atomic ticket update operations
    @Transactional
    // Updates ticket title, description, category, and priority verifying editing rules
    public TicketResponse updateTicket(Long id, TicketRequest request, Authentication authentication) {
        // Queries target ticket by ID throwing HTTP 404 if not found
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        // Restricts ticket editing exclusively to unassigned tickets in OPENED state
        if (ticket.getState() != TicketState.OPENED || ticket.getTechnicianAssigned() != null) {
            // Throws HTTP 409 conflict exception if ticket is assigned or beyond OPENED state
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden editar tickets ABIERTOS sin asignar");
        // Closes if condition checking ticket editing rules
        }

        // Verifies that non-admin authenticated users only edit tickets they created
        if (authentication != null && !hasRole(authentication, "ROLE_ADMIN")) {
            // Extracts username email of authenticated user from security context
            String email = authentication.getName();
            // Checks whether requester matches authenticated user's email address
            if (ticket.getRequester() == null || !email.equalsIgnoreCase(ticket.getRequester().getEmail())) {
                // Throws HTTP 403 forbidden exception if user is not the ticket requester
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el solicitante puede modificar su ticket");
            // Closes if condition checking requester identity
            }
        // Closes if condition checking non-admin permissions
        }

        // Updates ticket title attribute with new value from request
        ticket.setTitle(request.getTitle());
        // Updates ticket description attribute with new narrative from request
        ticket.setDescription(request.getDescription());
        // Updates ticket domain category classification from request
        ticket.setCategory(request.getCategory());
        // Updates ticket urgency priority rating from request
        ticket.setPriority(request.getPriority());

        // Persists updated ticket entity and maps to response DTO for return
        return TicketResponse.fromEntity(ticketRepository.save(ticket));
    // Closes updateTicket method
    }

    /**
     * Updates ticket information overload.
     */
    // Manages database transaction boundary for overloaded updateTicket method
    @Transactional
    // Overloaded method updating ticket without passing explicit authentication context
    public TicketResponse updateTicket(Long id, TicketRequest request) {
        // Delegates execution to main updateTicket method passing null authentication
        return updateTicket(id, request, null);
    // Closes overloaded updateTicket method
    }

    /**
     * Assigns technician to ticket.
     */
    // Manages database transaction boundary ensuring atomic technician assignment and history logging
    @Transactional
    // Assigns support technician to ticket advancing state to ASSIGNED and logging audit record
    public TicketResponse assignTechnician(Long id, Long technicianId, Authentication authentication) {
        // Queries target ticket by ID throwing HTTP 404 if not found
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        // Queries technician user entity by ID throwing HTTP 404 if not found
        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Técnico no encontrado"));

        // Checks whether selected user entity possesses TECHNICIAN role authority
        boolean isTechnician = technician.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.TECHNICIAN);
        // Validates that assigned user has the required technician role
        if (!isTechnician) {
            // Throws HTTP 400 bad request if user lacks TECHNICIAN authorization role
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El usuario seleccionado no tiene el rol de TÉCNICO y no puede ser asignado");
        // Closes if condition validating technician role
        }

        // Captures previous state of ticket before updating assignment
        TicketState previousState = ticket.getState();
        // Sets assigned technician entity on target ticket
        ticket.setTechnicianAssigned(technician);
        // Advances ticket lifecycle state to ASSIGNED status
        ticket.setState(TicketState.ASSIGNED);

        // Initializes variable to hold administrator user performing assignment
        User admin = null;
        // Checks whether authentication context was provided with assignment request
        if (authentication != null) {
            // Retrieves administrator user entity by authenticated principal email
            admin = userRepository.findByEmail(authentication.getName()).orElse(null);
        // Closes if condition checking authentication
        }
        // Assigns actor performing transition defaulting to technician if admin is null
        User changedBy = (admin != null) ? admin : technician;

        // Records technician assignment transition in immutable ticket history audit log
        ticketHistoryService.recordChange(ticket, previousState, TicketState.ASSIGNED, changedBy);
        // Persists updated ticket entity and returns mapped response DTO
        return TicketResponse.fromEntity(ticketRepository.save(ticket));
    // Closes assignTechnician method
    }

    /**
     * Assigns ticket technician overload.
     */
    // Manages database transaction boundary for overloaded technician assignment method
    @Transactional
    // Overloaded method assigning technician without passing explicit authentication context
    public TicketResponse assignTechnician(Long id, Long technicianId) {
        // Delegates to main assignTechnician method passing null authentication
        return assignTechnician(id, technicianId, null);
    // Closes overloaded assignTechnician method
    }

    /**
     * Transitions ticket lifecycle state.
     */
    // Manages database transaction boundary ensuring atomic state transition and history logging
    @Transactional
    // Validates business state transition rules and advances ticket to next lifecycle state
    public TicketResponse updateState(Long id, TicketState newState, Authentication authentication) {
        // Queries target ticket by ID throwing HTTP 404 if not found
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        // Captures current state of ticket before applying state transition
        TicketState previousState = ticket.getState();

        // Prevents modifying state of tickets that are already formally closed
        if (previousState == TicketState.CLOSED) {
            // Throws HTTP 409 conflict exception if ticket is already closed
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ticket ya está CERRADO");
        // Closes if condition checking closed state
        }

        // Enforces rule that ticket can only be RESOLVED if previously IN_PROCESS
        if (newState == TicketState.RESOLVED && previousState != TicketState.IN_PROCESS) {
            // Throws HTTP 409 conflict exception if resolving without prior IN_PROCESS state
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un ticket solo puede marcarse RESUELTA si previamente estuvo EN_PROCESO");
        // Closes if condition checking RESOLVED transition rule
        }

        // Validates closing transition rules ensuring ticket is RESOLVED and closed by owner
        if (newState == TicketState.CLOSED) {
            // Rejects closing if ticket has not yet reached RESOLVED state
            if (previousState != TicketState.RESOLVED) {
                // Throws HTTP 409 conflict exception if closing an unresolved ticket
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Un ticket solo puede cerrarse si previamente está en estado RESUELTA");
            // Closes if condition checking RESOLVED prior state
            }
            // Verifies requester closing permissions for non-admin authenticated users
            if (authentication != null && !hasRole(authentication, "ROLE_ADMIN")) {
                // Extracts username email of authenticated principal user
                String email = authentication.getName();
                // Verifies that requester created this ticket before allowing closure
                if (ticket.getRequester() == null || !email.equalsIgnoreCase(ticket.getRequester().getEmail())) {
                    // Throws HTTP 403 forbidden exception if user is not ticket owner
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el solicitante puede confirmar el cierre de su ticket");
                // Closes if condition checking requester ownership
                }
            // Closes if condition checking non-admin role
            }
        // Closes if condition validating CLOSED state transition
        }

        // Validates transition to IN_PROCESS ensuring ticket was ASSIGNED and technician matches
        if (newState == TicketState.IN_PROCESS) {
            // Rejects starting work if ticket was not previously in ASSIGNED state
            if (previousState != TicketState.ASSIGNED) {
                // Throws HTTP 409 conflict exception if moving to IN_PROCESS from unassigned state
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Un ticket solo puede pasar a EN_PROCESO desde ASIGNADA");
            // Closes if condition checking ASSIGNED prior state
            }
            // Verifies technician permissions for non-admin authenticated users
            if (authentication != null && !hasRole(authentication, "ROLE_ADMIN")) {
                // Extracts username email of authenticated principal user
                String email = authentication.getName();
                // Verifies that assigned technician matches current authenticated user
                if (ticket.getTechnicianAssigned() == null || !email.equalsIgnoreCase(ticket.getTechnicianAssigned().getEmail())) {
                    // Throws HTTP 403 forbidden exception if user is not assigned technician
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el técnico asignado puede iniciar este ticket");
                // Closes if condition checking technician email match
                }
            // Closes if condition checking non-admin role
            }
        // Closes if condition validating IN_PROCESS transition
        }

        // Validates transition to RESOLVED setting resolution timestamp upon completion
        if (newState == TicketState.RESOLVED) {
            // Verifies technician resolution permissions for non-admin authenticated users
            if (authentication != null && !hasRole(authentication, "ROLE_ADMIN")) {
                // Extracts username email of authenticated principal user
                String email = authentication.getName();
                // Verifies that assigned technician matches current authenticated user
                if (ticket.getTechnicianAssigned() == null || !email.equalsIgnoreCase(ticket.getTechnicianAssigned().getEmail())) {
                    // Throws HTTP 403 forbidden exception if user is not assigned technician
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el técnico asignado puede resolver este ticket");
                // Closes if condition checking technician email match
                }
            // Closes if condition checking non-admin role
            }
            // Records the resolution date and time on the ticket entity
            ticket.setResolutionDate(LocalDateTime.now());
        // Populates closing timestamp when ticket reaches CLOSED state
        } else if (newState == TicketState.CLOSED) {
            // Records the closing date and time on the ticket entity
            ticket.setCloseDate(LocalDateTime.now());
        // Closes else-if branch for CLOSED state
        }

        // Updates the ticket state attribute to the new validated lifecycle state
        ticket.setState(newState);

        // Initializes user reference representing actor performing this state transition
        User changer = null;
        // Looks up authenticated user entity if authentication context is available
        if (authentication != null) {
            // Queries user entity by authenticated principal email address
            changer = userRepository.findByEmail(authentication.getName()).orElse(null);
        // Closes if condition checking authentication presence
        }
        // Fallbacks changer to assigned technician or requester if authentication was null
        if (changer == null) {
            // Assigns technician if present otherwise defaults to requester user
            changer = ticket.getTechnicianAssigned() != null ? ticket.getTechnicianAssigned() : ticket.getRequester();
        // Closes if condition checking null changer
        }

        // Records the state transition in the immutable ticket history audit log
        ticketHistoryService.recordChange(ticket, previousState, newState, changer);
        // Persists updated ticket entity and returns mapped response DTO
        return TicketResponse.fromEntity(ticketRepository.save(ticket));
    // Closes updateState method
    }

    /**
     * Updates ticket state overload.
     */
    // Manages database transaction boundary for overloaded updateState method
    @Transactional
    // Overloaded method transitioning state without passing explicit authentication context
    public TicketResponse updateState(Long id, TicketState newState) {
        // Delegates to main updateState method passing null authentication
        return updateState(id, newState, null);
    // Closes overloaded updateState method
    }

    /**
     * Finds tickets by requester.
     */
    // Configures read-only transaction optimizing performance for querying requester tickets
    @Transactional(readOnly = true)
    // Retrieves list of tickets submitted by a specific requester user ID
    public List<TicketResponse> getTicketsByRequester(Long requesterId) {
        // Queries tickets by requester ID streaming and mapping each to response DTO
        return ticketRepository.findByRequesterId(requesterId).stream()
                .map(TicketResponse::fromEntity).collect(Collectors.toList());
    // Closes getTicketsByRequester method
    }

    /**
     * Finds tickets by technician.
     */
    // Configures read-only transaction optimizing performance for querying technician tickets
    @Transactional(readOnly = true)
    // Retrieves list of tickets assigned to a specific technician user ID
    public List<TicketResponse> getTicketsByTechnician(Long technicianId) {
        // Queries tickets by technician ID streaming and mapping each to response DTO
        return ticketRepository.findByTechnicianAssignedId(technicianId).stream()
                .map(TicketResponse::fromEntity).collect(Collectors.toList());
    // Closes getTicketsByTechnician method
    }

    /**
     * Deletes ticket by identifier.
     */
    // Manages database transaction boundary ensuring atomic ticket deletion operation
    @Transactional
    // Deletes support ticket entity by its database primary key identifier
    public void deleteTicket(Long id) {
        // Deletes ticket record and cascading child comments and histories from database
        ticketRepository.deleteById(id);
    // Closes deleteTicket method
    }

    /**
     * Checks ticket access permissions.
     */
    // Private helper method verifying whether user has permission to view ticket
    private boolean canAccessTicket(Ticket ticket, Authentication authentication) {
        // Grants unconditional ticket access to system administrators
        if (hasRole(authentication, "ROLE_ADMIN")) return true;
        // Extracts username email of authenticated user from security principal
        String email = authentication.getName();
        // Checks access for technicians verifying assignment to this ticket
        if (hasRole(authentication, "ROLE_TECHNICIAN")) {
            // Allows access if technician is assigned and matches current user's email
            return ticket.getTechnicianAssigned() != null &&
                    email.equalsIgnoreCase(ticket.getTechnicianAssigned().getEmail());
        // Closes if condition checking technician role
        }
        // Allows access for requesters if they created this support ticket
        return ticket.getRequester() != null &&
                email.equalsIgnoreCase(ticket.getRequester().getEmail());
    // Closes canAccessTicket method
    }

    /**
     * Checks user role authority.
     */
    // Private helper evaluating whether Authentication contains a specific granted authority
    private boolean hasRole(Authentication authentication, String role) {
        // Iterates through granted authorities associated with authenticated user
        for (GrantedAuthority ga : authentication.getAuthorities()) {
            // Compares granted authority string with target role name argument
            if (role.equals(ga.getAuthority())) {
                // Returns true immediately upon finding matching authority role
                return true;
            // Closes if condition checking role match
            }
        // Closes for loop iterating authorities
        }
        // Returns false when matching security authority is not found
        return false;
    // Closes hasRole helper method
    }

    private final TagRepository tagRepository;
      */
    @Transactional
    public TicketResponse addTagsToTicket(Long ticketId, List<String> tagNames, Authentication authentication) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket no encontrado con id: " + ticketId));

        // Validación de permisos: ADMIN o el TECHNICIAN asignado
        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
        boolean isAssignedTech = ticket.getTechnicianAssigned() != null &&
                ticket.getTechnicianAssigned().getEmail().equalsIgnoreCase(authentication.getName());

        if (!isAdmin && !isAssignedTech) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo un administrador o el técnico asignado pueden agregar etiquetas a este ticket");
        }

        if (tagNames == null || tagNames.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La lista de etiquetas no puede estar vacía");
        }

        for (String rawName : tagNames) {
            if (rawName == null || rawName.trim().isEmpty()) continue;

            // 1. Normalización estricta en mayúsculas sin espacios redundantes
            String normalized = rawName.trim().toUpperCase();

            // 2. Si la etiqueta no existe, se crea automáticamente; si existe, se vincula
            Tag tag = tagRepository.findByNombreIgnoreCase(normalized)
                    .orElseGet(() -> tagRepository.save(Tag.builder().nombre(normalized).build()));

            ticket.getTags().add(tag);
        }

        Ticket saved = ticketRepository.save(ticket);
        return TicketResponse.fromEntity(saved);
    }

    /**
     * Retorna tickets asociados a una etiqueta respetando la visibilidad por rol.
     */
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByTag(String nombre, Authentication authentication) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El parámetro 'nombre' de la etiqueta es obligatorio");
        }

        String normalized = nombre.trim().toUpperCase();
        List<Ticket> matchingTickets = ticketRepository.findByTagNombreIgnoreCase(normalized);

        // Si no hay autenticación, retorna todo (o restringe según configuración)
        if (authentication == null) {
            return matchingTickets.stream().map(TicketResponse::fromEntity).collect(Collectors.toList());
        }

        String email = authentication.getName();
        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
        boolean isTechnician = hasRole(authentication, "ROLE_TECHNICIAN");

        // Filtrado por visibilidad de rol:
        // ADMIN: ve todos
        // TECHNICIAN: solo si está asignado a él
        // USER: solo si es el creador (requester)
        return matchingTickets.stream()
                .filter(ticket -> {
                    if (isAdmin) return true;
                    if (isTechnician) {
                        return ticket.getTechnicianAssigned() != null &&
                                ticket.getTechnicianAssigned().getEmail().equalsIgnoreCase(email);
                    }
                    return ticket.getRequester() != null &&
                            ticket.getRequester().getEmail().equalsIgnoreCase(email);
                })
                .map(TicketResponse::fromEntity)
                .collect(Collectors.toList());
    }


// Closes TicketService class definition
}
