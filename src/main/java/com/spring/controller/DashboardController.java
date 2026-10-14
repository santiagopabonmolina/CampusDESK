package com.spring.controller;

import com.spring.dto.response.DashboardResponse;
import com.spring.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "4. Panel de Indicadores y Métricas", description = "Estadísticas agregadas en tiempo real de incidencias desde PostgreSQL")
@RestController
@RequestMapping({"/api/reports", "/api/dashboard"})
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Obtener indicadores generales", description = "Calcula conteos de tickets totales, abiertos, asignados, en proceso, resueltos y cerrados con filtro automático por rol.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Métricas calculadas con éxito"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping({"/summary", "/stats"})
    public ResponseEntity<DashboardResponse> getSummary(Authentication authentication) {
        return ResponseEntity.ok(dashboardService.getDashboardSummary(authentication));
    }
}
