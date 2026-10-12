package com.spring.controller;

import com.spring.entity.User;
import com.spring.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "3. Directorio de Usuarios", description = "Consulta de técnicos disponibles y gestión del personal registrado")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Listar técnicos disponibles", description = "Retorna la lista de usuarios activos que cuentan con el rol TECHNICIAN para asignación de incidencias.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de técnicos obtenido con éxito"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/technicians")
    public ResponseEntity<List<User>> getTechnicians() {
        return ResponseEntity.ok(userService.getAvailableTechnicians());
    }

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
}
