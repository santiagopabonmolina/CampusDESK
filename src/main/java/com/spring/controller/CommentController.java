// Defines the package namespace containing Spring MVC REST controllers
package com.spring.controller;

// Imports CommentRequest DTO containing payload data for creating comments
import com.spring.dto.request.CommentRequest;
// Imports CommentResponse DTO transferring comment data to frontend clients
import com.spring.dto.response.CommentResponse;
// Imports CommentService handling business logic for ticket comments
import com.spring.service.CommentService;
// Imports Swagger Operation annotation describing REST endpoint operation details
import io.swagger.v3.oas.annotations.Operation;
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
// Imports Spring Security Authentication representing the authenticated user principal
import org.springframework.security.core.Authentication;
// Imports Spring MVC annotations for mapping HTTP methods and endpoints
import org.springframework.web.bind.annotation.*;

// Imports Java utility List collection interface for handling sequential lists
import java.util.List;

// Categorizes comment endpoints under section six in OpenAPI documentation UI
@Tag(name = "6. Sistema de Comentarios", description = "Endpoints alternativos para gestión de comentarios")
// Marks this class as REST controller serializing responses directly to JSON
@RestController
// Maps base URL route for comment endpoints to api comments path
@RequestMapping("/api/comments")
// Generates constructor injecting required final CommentService dependency automatically
@RequiredArgsConstructor
// Declares CommentController exposing HTTP endpoints for managing ticket comments
public class CommentController {

    // Declares final business service reference for executing comment operations
    private final CommentService commentService;

    /**
     * Creates a ticket comment.
     */
    // Documents general comment registration endpoint behavior in Swagger UI
    @Operation(summary = "Registrar comentario general", description = "Crea un comentario para un ticket activo.")
    // Maps HTTP POST requests on base endpoint to this handler method
    @PostMapping
    // Creates a new comment validating payload and enforcing permission checks
    public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CommentRequest request,
                                                         Authentication authentication) {
        // Delegates comment creation to service passing request and authentication context
        CommentResponse response = commentService.createComment(request, authentication);
        // Returns HTTP 201 created status with created comment response payload
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    // Closes createComment handler method
    }

    /**
     * Retrieves ticket comments list.
     */
    // Documents ticket comment listing endpoint behavior in Swagger UI
    @Operation(summary = "Listar comentarios por ID de ticket", description = "Obtiene los comentarios de un ticket.")
    // Maps HTTP GET requests on ticket ID sub-path to this handler method
    @GetMapping("/ticket/{ticketId}")
    // Retrieves chronologically ordered list of comments for specified ticket ID
    public ResponseEntity<List<CommentResponse>> getCommentsByTicket(@PathVariable Long ticketId) {
        // Retrieves comment list from service for the specified ticket ID
        List<CommentResponse> responses = commentService.getCommentsByTicketId(ticketId);
        // Returns HTTP 200 OK containing list of comment response DTOs
        return ResponseEntity.ok(responses);
    // Closes getCommentsByTicket handler method
    }
// Closes CommentController class definition
}
