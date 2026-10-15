package com.spring.controller;

import com.spring.entity.User;
import com.spring.entity.enums.RoleName;
import com.spring.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Tag(name = "3. Directorio de Usuarios", description = "Consulta de técnicos disponibles y gestión del personal registrado")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Retrieves available technician users.
     */
    @Operation(summary = "Listar técnicos disponibles", description = "Retorna la lista de usuarios activos que cuentan con el rol TECHNICIAN para asignación de incidencias.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de técnicos obtenido con éxito"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/technicians")
    public ResponseEntity<List<User>> getTechnicians() {
        return ResponseEntity.ok(userService.getAvailableTechnicians());
    }

    /**
     * Retrieves all registered users.
     */
    @Operation(summary = "Listar todos los usuarios (Solo ADMIN)", description = "Permite al administrador consultar el directorio completo de usuarios registrados en TechNova Solutions.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de usuarios"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Updates assigned user roles.
     */
    @Operation(summary = "Actualizar roles de un usuario (Solo ADMIN)", description = "Permite a un administrador asignar y desasignar roles (ADMIN, TECHNICIAN, USER) a cualquier colaborador en la base de datos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roles actualizados exitosamente en PostgreSQL"),
            @ApiResponse(responseCode = "400", description = "Lista de roles vacía o inválida"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUserRoles(
            @Parameter(description = "ID del usuario a modificar") @PathVariable Long id,
            @Parameter(description = "Conjunto de roles a asignar (ej. [\"USER\", \"TECHNICIAN\"])") @RequestBody Set<RoleName> roles) {
        return ResponseEntity.ok(userService.updateUserRoles(id, roles));
    }
}
