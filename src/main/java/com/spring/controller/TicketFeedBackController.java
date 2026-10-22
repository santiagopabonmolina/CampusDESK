package com.spring.controller;

import com.spring.dto.response.DashboardResponse;
import com.spring.dto.response.TicketFeedBackResponse;
import com.spring.service.TicketFeedBackService;
import io.swagger.v3.oas.annotations.Operation;
// Imports User JPA entity representing user account records
import com.spring.entity.User;
// Imports RoleName enum representing supported system authorization roles
import com.spring.entity.enums.RoleName;
// Imports UserService handling business workflows for user management
import com.spring.service.UserService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "feedBack técnico (Tickets)", description = "Control completo del ciclo de calificacion tickets con control de acceso basado en roles (RBAC)")
// Marks this class as REST controller serializing responses directly to JSON
@RestController
// Maps base URL route for ticket endpoints to api tickets path
@RequestMapping("/api)
// Generates constructor injecting required final service dependencies automatically
@RequiredArgsConstructor
// Declares TicketController exposing RESTful HTTP endpoints for support ticket management

public class TicketFeedBackController {

    @Operation(summary = "Clasificar", description = "Crea una clasificacion")
    @PostMapping("/tickets/{id}/feedback")
    @PreAuthorize("hasRole('USER')")

    @ApiResponses({
            @ApiResponse(responseCode = "409", description = "CONFLICT"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST")
    })



    @GetMapping("/technicians/{id}/promedio-calificacion")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketFeedBackResponse> getAverage(Authentication authentication) {
        // Delegates metric calculations to service and returns HTTP 200 OK
        return ResponseEntity.ok(TicketFeedBackService.getAverage(authentication));
        // Closes getSummary handler method
    }

}
