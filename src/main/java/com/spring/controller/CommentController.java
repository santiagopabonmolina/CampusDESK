package com.spring.controller;

import com.spring.dto.request.CommentRequest;
import com.spring.dto.response.CommentResponse;
import com.spring.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "6. Sistema de Comentarios", description = "Endpoints alternativos para gestión de comentarios")
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Registrar comentario general", description = "Crea un comentario para un ticket activo.")
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CommentRequest request,
                                                         Authentication authentication) {
        CommentResponse response = commentService.createComment(request, authentication);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Listar comentarios por ID de ticket", description = "Obtiene los comentarios de un ticket.")
    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByTicket(@PathVariable Long ticketId) {
        List<CommentResponse> responses = commentService.getCommentsByTicketId(ticketId);
        return ResponseEntity.ok(responses);
    }
}
