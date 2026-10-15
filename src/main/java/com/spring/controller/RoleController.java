// Defines the package namespace containing Spring MVC REST controllers
package com.spring.controller;

// Imports RoleRequest DTO carrying payload data for creating roles
import com.spring.dto.request.RoleRequest;
// Imports RoleResponse DTO transferring role entity information to clients
import com.spring.dto.response.RoleResponse;
// Imports RoleService handling business logic for system authorization roles
import com.spring.service.RoleService;
// Imports Swagger Operation annotation describing REST endpoint operation details
import io.swagger.v3.oas.annotations.Operation;
// Imports Swagger Tag annotation categorizing controllers in OpenAPI documentation
import io.swagger.v3.oas.annotations.tags.Tag;
// Imports Lombok annotation generating constructor for final dependency fields
import lombok.RequiredArgsConstructor;
// Imports HttpStatus enum representing standard HTTP status response codes
import org.springframework.http.HttpStatus;
// Imports ResponseEntity representing full HTTP responses including headers and body
import org.springframework.http.ResponseEntity;
// Imports PreAuthorize annotation enforcing method-level Spring Security role checks
import org.springframework.security.access.prepost.PreAuthorize;
// Imports Spring MVC annotations for mapping HTTP methods and endpoints
import org.springframework.web.bind.annotation.*;

// Imports Java utility List collection interface for returning sequential lists
import java.util.List;

// Categorizes role management endpoints under section seven in OpenAPI UI
@Tag(name = "7. Catálogo de Roles", description = "Gestión y consulta de los roles del sistema (Solo ADMIN)")
// Marks this class as REST controller serializing responses directly to JSON
@RestController
// Maps base URL route for role endpoints to api roles path
@RequestMapping("/api/roles")
// Generates constructor injecting required final RoleService dependency automatically
@RequiredArgsConstructor
// Declares RoleController exposing endpoints for administrator role management
public class RoleController {

    // Declares final business service reference for managing role entities
    private final RoleService roleService;

    /**
     * Creates a new role.
     */
    // Documents role creation endpoint purpose and restrictions in Swagger
    @Operation(summary = "Crear nuevo rol", description = "Crea un nuevo rol en la base de datos.")
    // Maps HTTP POST requests on base endpoint to role creation method
    @PostMapping
    // Enforces role-based security requiring ADMIN authority to create roles
    @PreAuthorize("hasRole('ADMIN')")
    // Creates a new role validating unique existence and returning HTTP 201
    public ResponseEntity<RoleResponse> createRole(@RequestBody RoleRequest request) {
        // Delegates role creation to business service passing request payload
        RoleResponse response = roleService.createRole(request);
        // Returns HTTP 201 created status with created role response payload
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    // Closes createRole handler method
    }

    /**
     * Retrieves all system roles.
     */
    // Documents role catalog listing endpoint behavior in Swagger UI
    @Operation(summary = "Listar roles", description = "Retorna todos los roles configurados.")
    // Maps HTTP GET requests on base endpoint to role listing method
    @GetMapping
    // Enforces role-based security requiring ADMIN authority to view roles
    @PreAuthorize("hasRole('ADMIN')")
    // Queries and returns list of all roles configured in database
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        // Retrieves full list of roles from business service layer
        List<RoleResponse> responses = roleService.getAllRoles();
        // Returns HTTP 200 OK containing list of role response DTOs
        return ResponseEntity.ok(responses);
    // Closes getAllRoles handler method
    }
// Closes RoleController class definition
}
