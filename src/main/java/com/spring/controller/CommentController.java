package com.spring.controller;

import com.spring.dto.request.CommentRequest;
import com.spring.dto.response.CommentResponse;
import com.spring.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @RequestBody CommentRequest request,
            Authentication authentication) {
        CommentResponse response = commentService.createComment(request, authentication);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByTicket(
            @PathVariable Long ticketId,
            Authentication authentication) {
        List<CommentResponse> responses = commentService.getCommentsByTicketId(ticketId, authentication);
        return ResponseEntity.ok(responses);
    }
}
