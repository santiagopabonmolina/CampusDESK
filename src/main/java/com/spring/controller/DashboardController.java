// Defines the package namespace containing Spring MVC REST controllers
package com.spring.controller;

// Imports DashboardResponse DTO carrying aggregated metrics to frontend dashboard
import com.spring.dto.response.DashboardResponse;
// Imports DashboardService calculating role-based metric aggregations
import com.spring.service.DashboardService;
// Imports Swagger Operation annotation describing REST endpoint functionality
import io.swagger.v3.oas.annotations.Operation;
// Imports Swagger ApiResponse annotation detailing individual HTTP responses
import io.swagger.v3.oas.annotations.responses.ApiResponse;
// Imports Swagger ApiResponses annotation grouping multiple API response descriptions
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// Imports Swagger Tag annotation categorizing controllers in OpenAPI documentation
import io.swagger.v3.oas.annotations.tags.Tag;
// Imports Lombok annotation generating constructor for final dependency fields
import lombok.RequiredArgsConstructor;
// Imports ResponseEntity representing full HTTP responses including status and body
import org.springframework.http.ResponseEntity;
// Imports Spring Security Authentication representing the authenticated user principal
import org.springframework.security.core.Authentication;
// Imports GetMapping annotation mapping HTTP GET requests onto handler methods
import org.springframework.web.bind.annotation.GetMapping;
// Imports RequestMapping annotation mapping web requests onto controller classes
import org.springframework.web.bind.annotation.RequestMapping;
// Imports RestController annotation declaring this class as RESTful controller bean
import org.springframework.web.bind.annotation.RestController;

// Categorizes metrics endpoints under section four in OpenAPI documentation UI
@Tag(name = "4. Panel de Indicadores y Métricas", description = "Estadísticas agregadas en tiempo real de incidencias desde PostgreSQL")
// Marks this class as REST controller serializing responses directly to JSON
@RestController
// Maps dual URL route paths api reports and api dashboard to this controller
@RequestMapping({"/api/reports", "/api/dashboard"})
// Generates constructor injecting required final DashboardService dependency automatically
@RequiredArgsConstructor
// Declares DashboardController exposing endpoints for ticket summary metrics and reports
public class DashboardController {

    // Declares final business service reference for computing dashboard metrics
    private final DashboardService dashboardService;

    /**
     * Returns dashboard metrics summary.
     */
    // Documents general indicators endpoint behavior and role filtering in Swagger
    @Operation(summary = "Obtener indicadores generales", description = "Calcula conteos de tickets totales, abiertos, asignados, en proceso, resueltos y cerrados con filtro automático por rol.")
    // Configures OpenAPI responses for successful computation and unauthenticated requests
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Métricas calculadas con éxito"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    // Maps HTTP GET requests on paths summary and stats to this handler
    @GetMapping({"/summary", "/stats"})
    // Computes and returns aggregated dashboard statistics tailored to user's role
    public ResponseEntity<DashboardResponse> getSummary(Authentication authentication) {
        // Delegates metric calculations to service and returns HTTP 200 OK
        return ResponseEntity.ok(dashboardService.getDashboardSummary(authentication));
    // Closes getSummary handler method
    }
// Closes DashboardController class definition
}
