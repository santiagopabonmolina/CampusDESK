// Defines the package namespace containing Spring MVC REST controllers
package com.spring.controller;

// Imports CommentRequest DTO carrying comment submission payload data
import com.spring.dto.request.CommentRequest;
// Imports TicketRequest DTO carrying ticket creation and update payload data
import com.spring.dto.request.TicketRequest;
// Imports CommentResponse DTO transferring comment data to frontend clients
import com.spring.dto.response.CommentResponse;
// Imports TicketResponse DTO transferring ticket entity information to presentation
import com.spring.dto.response.TicketResponse;
// Imports TicketCategory enum specifying incident domain classification categories
import com.spring.entity.enums.TicketCategory;
// Imports TicketPriority enum specifying incident urgency priority levels
import com.spring.entity.enums.TicketPriority;
// Imports TicketState enum specifying ticket workflow lifecycle states
import com.spring.entity.enums.TicketState;
// Imports CommentService handling business logic for ticket comments
import com.spring.service.CommentService;
// Imports TicketService handling business logic and workflows for tickets
import com.spring.service.TicketService;
// Imports Swagger Operation annotation describing REST endpoint operation details
import io.swagger.v3.oas.annotations.Operation;
// Imports Swagger Parameter annotation documenting request parameters in Swagger
import io.swagger.v3.oas.annotations.Parameter;
// Imports Swagger ApiResponse annotation detailing individual HTTP responses
import io.swagger.v3.oas.annotations.responses.ApiResponse;
// Imports Swagger ApiResponses annotation grouping multiple API response descriptions
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// Imports Swagger Tag annotation categorizing controllers in OpenAPI documentation
import io.swagger.v3.oas.annotations.tags.Tag;
// Imports Jakarta Valid annotation triggering constraint validation on request body
import jakarta.validation.Valid;
// Imports Lombok annotation generating constructor for final dependency fields
import lombok.RequiredArgsConstructor;
// Imports HttpStatus enum representing standard HTTP status response codes
import org.springframework.http.HttpStatus;
// Imports ResponseEntity representing full HTTP responses including headers and body
import org.springframework.http.ResponseEntity;
// Imports PreAuthorize annotation enforcing method-level Spring Security role checks
import org.springframework.security.access.prepost.PreAuthorize;
// Imports Spring Security Authentication interface representing authenticated principal
import org.springframework.security.core.Authentication;
// Imports Spring MVC annotations for mapping HTTP methods and endpoints
import org.springframework.web.bind.annotation.*;

// Imports Java utility List collection interface for returning sequential lists
import java.util.List;

// Categorizes ticket endpoints under section two in OpenAPI documentation UI
@Tag(name = "2. Incidencias Tecnológicas (Tickets)", description = "Control completo del ciclo de vida de tickets con control de acceso basado en roles (RBAC)")
// Marks this class as REST controller serializing responses directly to JSON
@RestController
// Maps base URL route for ticket endpoints to api tickets path
@RequestMapping("/api/tickets")
// Generates constructor injecting required final service dependencies automatically
@RequiredArgsConstructor
// Declares TicketController exposing RESTful HTTP endpoints for support ticket management
public class TicketController {

    // Declares final business service reference for ticket lifecycle operations
    private final TicketService ticketService;
    // Declares final business service reference for ticket comment operations
    private final CommentService commentService;

    /**
     * Creates a new ticket.
     */
    // Documents ticket creation endpoint purpose and initial state in Swagger
    @Operation(summary = "Crear nuevo ticket", description = "Registra una nueva incidencia con estado inicial ABIERTA (OPENED) y prioridad/categoría especificadas.")
    // Configures OpenAPI responses for successful creation, bad input, and unauthenticated
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ticket creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    // Maps HTTP POST requests on base endpoint to ticket creation method
    @PostMapping
    // Handles HTTP POST request creating new ticket and returning HTTP 201
    public ResponseEntity<TicketResponse> createTicket(
            // Validates request body constraints before binding TicketRequest object
            @Valid @RequestBody TicketRequest request) {
        // Delegates ticket creation to service layer passing request payload
        TicketResponse response = ticketService.createTicket(request);
        // Returns HTTP 201 created status with created ticket response payload
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    // Closes createTicket handler method
    }

    /**
     * Lists all accessible tickets.
     */
    // Documents ticket listing endpoint with role isolation and filter rules
    @Operation(summary = "Listar tickets autorizados", description = "Lista tickets con aislamiento de roles: ADMIN ve todos, TECHNICIAN ve sus asignados, USER ve solo sus solicitudes creadas. Permite filtrar por estado/status, prioridad y categoría.")
    // Configures OpenAPI responses for successful list retrieval and unauthenticated access
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de incidencias devuelto con éxito"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    // Maps HTTP GET requests on base endpoint to ticket listing method
    @GetMapping
    // Retrieves tickets applying optional query filters and role-based visibility restrictions
    public ResponseEntity<List<TicketResponse>> getAllTickets(
            // Documents optional state query filter parameter in Swagger UI
            @Parameter(description = "Filtrar por estado: OPENED, ASSIGNED, IN_PROCESS, RESOLVED, CLOSED")
            // Binds optional state request query parameter to TicketState enum
            @RequestParam(name = "state", required = false) TicketState state,
            // Documents optional status alias parameter in Swagger UI
            @Parameter(description = "Alias para filtrar por estado: OPENED, ASSIGNED, IN_PROCESS, RESOLVED, CLOSED")
            // Binds optional status request query parameter to TicketState enum
            @RequestParam(name = "status", required = false) TicketState status,
            // Documents optional priority query filter parameter in Swagger UI
            @Parameter(description = "Filtrar por prioridad: LOW, MEDIUM, HIGH, CRITIC")
            // Binds optional priority request query parameter to TicketPriority enum
            @RequestParam(required = false) TicketPriority priority,
            // Documents optional category query filter parameter in Swagger UI
            @Parameter(description = "Filtrar por categoría: HARDWARE, SOFTWARE, NETWORK, ACCESS, OTHER")
            // Binds optional category request query parameter to TicketCategory enum
            @RequestParam(required = false) TicketCategory category,
            // Injects Spring Security authentication principal for role and identity checks
            Authentication authentication) {
        // Resolves state filter prioritizing state parameter over status query alias
        TicketState filterState = (state != null) ? state : status;
        // Retrieves filtered tickets from service and returns HTTP 200 OK
        return ResponseEntity.ok(ticketService.getAllTickets(filterState, priority, category, authentication));
    // Closes getAllTickets handler method
    }

    /**
     * Retrieves ticket by identifier.
     */
    // Documents ticket detail retrieval endpoint and security permission checks
    @Operation(summary = "Consultar detalle de un ticket", description = "Obtiene información completa de la incidencia. Valida que el usuario tenga permisos sobre el recurso.")
    // Configures OpenAPI responses for success, unauthorized, forbidden, and not found
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle del ticket obtenido"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado a tickets ajenos"),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    // Maps HTTP GET requests with ID path variable to this handler
    @GetMapping("/{id}")
    // Retrieves detailed ticket data validating user authorization to access record
    public ResponseEntity<TicketResponse> getTicketById(
            // Binds ticket ID URL path variable to method Long parameter
            @PathVariable Long id,
            // Injects Spring Security authentication context for resource ownership checks
            Authentication authentication) {
        // Retrieves ticket from service verifying permissions and returns HTTP 200
        return ResponseEntity.ok(ticketService.getTicketById(id, authentication));
    // Closes getTicketById handler method
    }

    /**
     * Updates open ticket details.
     */
    // Documents ticket modification endpoint behavior and state editing limitations
    @Operation(summary = "Modificar solicitud abierta", description = "Permite actualizar título, descripción, prioridad o categoría. Solo permitido mientras el ticket esté en estado ABIERTA y sin asignar.")
    // Configures OpenAPI responses for successful update, forbidden, and state conflict
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket actualizado"),
            @ApiResponse(responseCode = "403", description = "No es el propietario del ticket"),
            @ApiResponse(responseCode = "409", description = "El ticket ya no está en estado ABIERTA o ya fue asignado")
    })
    // Maps HTTP PUT requests with ID path variable to this handler
    @PutMapping("/{id}")
    // Updates ticket attributes validating ownership and unassigned open ticket state
    public ResponseEntity<TicketResponse> updateTicket(
            // Binds target ticket numeric ID from URL path to parameter
            @PathVariable Long id,
            // Validates request body constraints before binding TicketRequest object
            @Valid @RequestBody TicketRequest request,
            // Injects Spring Security authentication context for ownership verification
            Authentication authentication) {
        // Delegates update to service validating constraints and returns HTTP 200
        return ResponseEntity.ok(ticketService.updateTicket(id, request, authentication));
    // Closes updateTicket handler method
    }

    /**
     * Assigns technician to ticket.
     */
    // Documents administrator technician assignment endpoint behavior and status transition
    @Operation(summary = "Asignar o reasignar técnico", description = "Exclusivo de ADMIN. Asigna un técnico y cambia el estado a ASIGNADA.")
    // Configures OpenAPI responses for successful assignment, bad input, and forbidden
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Técnico asignado exitosamente"),
            @ApiResponse(responseCode = "400", description = "El usuario seleccionado no tiene rol TECHNICIAN"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN")
    })
    // Maps HTTP PATCH requests on assign sub-path to this handler
    @PatchMapping("/{id}/assign")
    // Restricts endpoint invocation exclusively to users possessing the ADMIN role
    @PreAuthorize("hasRole('ADMIN')")
    // Assigns specified technician to ticket advancing lifecycle state to ASSIGNED
    public ResponseEntity<TicketResponse> assignTechnician(
            // Binds target ticket identifier from URL path to parameter
            @PathVariable Long id,
            // Binds technicianId request query parameter specifying target technician ID
            @RequestParam Long technicianId,
            // Injects authentication context to identify administrator assigning ticket
            Authentication authentication) {
        // Delegates assignment to service layer and returns HTTP 200 response
        return ResponseEntity.ok(ticketService.assignTechnician(id, technicianId, authentication));
    // Closes assignTechnician handler method
    }

    /**
     * Updates ticket lifecycle state.
     */
    // Documents lifecycle status transition endpoint and state progression rules
    @Operation(summary = "Transición de estado del ciclo de vida", description = "Valida reglas de negocio estrictas: ABIERTA ➔ ASIGNADA ➔ EN_PROCESO ➔ RESUELTA ➔ CERRADA.")
    // Configures OpenAPI responses for successful transition, forbidden, and conflict
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado con éxito"),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes para esta transición"),
            @ApiResponse(responseCode = "409", description = "Transición inválida o ticket ya cerrado")
    })
    // Maps HTTP PATCH requests on status sub-path to this handler
    @PatchMapping("/{id}/status")
    // Advances ticket lifecycle state validating business transition rules and actor
    public ResponseEntity<TicketResponse> changeStatus(
            // Binds target ticket identifier from URL path to parameter
            @PathVariable Long id,
            // Binds optional status request query parameter to TicketState enum
            @RequestParam(name = "status", required = false) TicketState status,
            // Binds optional state request query parameter to TicketState enum
            @RequestParam(name = "state", required = false) TicketState state,
            // Injects authentication context identifying actor initiating state transition
            Authentication authentication) {
        // Resolves target state prioritizing status parameter over state query alias
        TicketState targetState = (status != null) ? status : state;
        // Delegates state transition to service layer and returns HTTP 200
        return ResponseEntity.ok(ticketService.updateState(id, targetState, authentication));
    // Closes changeStatus handler method
    }

    /**
     * Deletes ticket from database.
     */
    // Documents ticket deletion endpoint behavior and administrator requirement in Swagger
    @Operation(summary = "Eliminar ticket", description = "Exclusivo de ADMIN. Elimina una incidencia y sus registros asociados.")
    // Configures OpenAPI responses for successful deletion and forbidden access
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ticket eliminado"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN")
    })
    // Maps HTTP DELETE requests with ID path variable to this handler
    @DeleteMapping("/{id}")
    // Restricts ticket deletion exclusively to users possessing the ADMIN role
    @PreAuthorize("hasRole('ADMIN')")
    // Deletes support ticket and child entities returning HTTP 204 No Content
    public ResponseEntity<Void> deleteTicket(
            // Binds target ticket identifier from URL path to parameter
            @PathVariable Long id) {
        // Delegates ticket deletion to business service layer by identifier
        ticketService.deleteTicket(id);
        // Builds and returns HTTP 204 No Content response entity
        return ResponseEntity.noContent().build();
    // Closes deleteTicket handler method
    }

    /**
     * Retrieves comments for ticket.
     */
    // Documents ticket comments listing endpoint behavior in Swagger UI
    @Operation(summary = "Consultar comentarios de un ticket", description = "Retorna la conversación cronológica asociada a la incidencia.")
    // Maps HTTP GET requests on comments sub-path to this handler method
    @GetMapping("/{id}/comments")
    // Retrieves chronologically ordered comments for ticket specified in URL path
    public ResponseEntity<List<CommentResponse>> getCommentsByTicketId(
            // Binds ticket identifier from URL path variable to parameter
            @PathVariable Long id) {
        // Queries comments from service layer and returns HTTP 200 OK
        return ResponseEntity.ok(commentService.getCommentsByTicketId(id));
    // Closes getCommentsByTicketId handler method
    }

    /**
     * Adds comment to ticket.
     */
    // Documents ticket comment addition endpoint behavior and closed ticket restriction
    @Operation(summary = "Agregar comentario a ticket", description = "Registra una nota o respuesta. Rechaza comentarios si el ticket está CERRADO.")
    // Maps HTTP POST requests on comments sub-path to this handler method
    @PostMapping("/{id}/comments")
    // Adds comment to ticket setting ticketId and validating requester permissions
    public ResponseEntity<CommentResponse> createComment(
            // Binds target ticket identifier from URL path to parameter
            @PathVariable Long id,
            // Validates request body constraints before binding CommentRequest object
            @Valid @RequestBody CommentRequest request,
            // Injects Spring Security authentication context identifying commenting author
            Authentication authentication) {
        // Sets target ticketId on request DTO from URL path variable
        request.setTicketId(id);
        // Delegates comment creation to service and returns HTTP 201 Created
        return new ResponseEntity<>(commentService.createComment(request, authentication), HttpStatus.CREATED);
    // Closes createComment handler method
    }

    @Operation(summary = "Asociar etiquetas a un ticket", description = "Asocia una lista de tags al ticket. Solo ADMIN o el técnico asignado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Etiquetas asociadas exitosamente"),
            @ApiResponse(responseCode = "400", description = "Lista de etiquetas inválida"),
            @ApiResponse(responseCode = "403", description = "No autorizado para etiquetar este ticket"),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    @PostMapping("/{id}/tags")
    public ResponseEntity<TicketResponse> addTags(
            @PathVariable Long id,
            @RequestBody List<String> tags,
            Authentication authentication) {
        return ResponseEntity.ok(ticketService.addTagsToTicket(id, tags, authentication));
    }

    @Operation(summary = "Consultar tickets por etiqueta", description = "Lista los tickets con la etiqueta indicada, respetando la visibilidad del rol.")
    @GetMapping("/por-tag")
    public ResponseEntity<List<TicketResponse>> getTicketsByTag(
            @RequestParam("nombre") String nombre,
            Authentication authentication) {
        return ResponseEntity.ok(ticketService.getTicketsByTag(nombre, authentication));

    }
// Closes TicketController class definition
}
