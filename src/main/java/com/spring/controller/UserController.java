// Defines the package namespace containing Spring MVC REST controllers
package com.spring.controller;

// Imports User JPA entity representing user account records
import com.spring.entity.User;
// Imports RoleName enum representing supported system authorization roles
import com.spring.entity.enums.RoleName;
// Imports UserService handling business workflows for user management
import com.spring.service.UserService;
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
// Imports Lombok annotation generating constructor for final dependency fields
import lombok.RequiredArgsConstructor;
// Imports ResponseEntity representing full HTTP responses including headers and body
import org.springframework.http.ResponseEntity;
// Imports PreAuthorize annotation enforcing method-level Spring Security role checks
import org.springframework.security.access.prepost.PreAuthorize;
// Imports Spring MVC annotations for mapping HTTP methods and endpoints
import org.springframework.web.bind.annotation.*;

// Imports Java utility List collection interface for returning sequential lists
import java.util.List;
// Imports Set interface representing an unordered collection of unique elements
import java.util.Set;

// Categorizes user directory endpoints under section three in OpenAPI UI
@Tag(name = "3. Directorio de Usuarios", description = "Consulta de técnicos disponibles y gestión del personal registrado")
// Marks this class as REST controller serializing responses directly to JSON
@RestController
// Maps base URL route for user directory endpoints to api users path
@RequestMapping("/api/users")
// Generates constructor injecting required final UserService dependency automatically
@RequiredArgsConstructor
// Declares UserController exposing RESTful endpoints for technician lookup and user role management
public class UserController {

    // Declares final business service reference for managing user accounts
    private final UserService userService;

    /**
     * Retrieves available technician users.
     */
    // Documents available technicians listing endpoint behavior in Swagger UI
    @Operation(summary = "Listar técnicos disponibles", description = "Retorna la lista de usuarios activos que cuentan con el rol TECHNICIAN para asignación de incidencias.")
    // Configures OpenAPI responses for successful retrieval and unauthenticated requests
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de técnicos obtenido con éxito"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    // Maps HTTP GET requests on technicians sub-path to this handler method
    @GetMapping("/technicians")
    // Queries and returns list of active users possessing TECHNICIAN role
    public ResponseEntity<List<User>> getTechnicians() {
        // Retrieves active technician list from service and returns HTTP 200
        return ResponseEntity.ok(userService.getAvailableTechnicians());
    // Closes getTechnicians handler method
    }

    /**
     * Retrieves all registered users.
     */
    // Documents system-wide user listing endpoint restricted to administrator role
    @Operation(summary = "Listar todos los usuarios (Solo ADMIN)", description = "Permite al administrador consultar el directorio completo de usuarios registrados en TechNova Solutions.")
    // Configures OpenAPI responses for successful user listing and forbidden access
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de usuarios"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)")
    })
    // Maps HTTP GET requests on base endpoint to user directory listing
    @GetMapping
    // Restricts directory listing access exclusively to users with ADMIN role
    @PreAuthorize("hasRole('ADMIN')")
    // Retrieves and returns complete list of registered users in system
    public ResponseEntity<List<User>> getAllUsers() {
        // Retrieves all user records from service and returns HTTP 200
        return ResponseEntity.ok(userService.getAllUsers());
    // Closes getAllUsers handler method
    }

    /**
     * Updates assigned user roles.
     */
    // Documents administrator role update endpoint modifying user permissions in PostgreSQL
    @Operation(summary = "Actualizar roles de un usuario (Solo ADMIN)", description = "Permite a un administrador asignar y desasignar roles (ADMIN, TECHNICIAN, USER) a cualquier colaborador en la base de datos.")
    // Configures OpenAPI responses for successful role update, bad input, forbidden, and not found
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roles actualizados exitosamente en PostgreSQL"),
            @ApiResponse(responseCode = "400", description = "Lista de roles vacía o inválida"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    // Maps HTTP PUT requests on roles sub-path to this handler
    @PutMapping("/{id}/roles")
    // Restricts role update operations exclusively to users with ADMIN role
    @PreAuthorize("hasRole('ADMIN')")
    // Updates assigned roles set for user returning updated user entity
    public ResponseEntity<User> updateUserRoles(
            // Documents target user identifier parameter in Swagger and binds path variable
            @Parameter(description = "ID del usuario a modificar") @PathVariable Long id,
            // Documents roles set parameter in Swagger and binds request JSON body
            @Parameter(description = "Conjunto de roles a asignar (ej. [\"USER\", \"TECHNICIAN\"])") @RequestBody Set<RoleName> roles) {
        // Delegates role updating to business service and returns HTTP 200
        return ResponseEntity.ok(userService.updateUserRoles(id, roles));
    // Closes updateUserRoles handler method
    }
// Closes UserController class definition
}
