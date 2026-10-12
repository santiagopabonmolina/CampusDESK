package com.spring.controller;

import com.spring.dto.request.RoleRequest;
import com.spring.dto.response.RoleResponse;
import com.spring.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "7. Catálogo de Roles", description = "Gestión y consulta de los roles del sistema (Solo ADMIN)")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Crear nuevo rol", description = "Crea un nuevo rol en la base de datos.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> createRole(@RequestBody RoleRequest request) {
        RoleResponse response = roleService.createRole(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Listar roles", description = "Retorna todos los roles configurados.")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> responses = roleService.getAllRoles();
        return ResponseEntity.ok(responses);
    }
}
