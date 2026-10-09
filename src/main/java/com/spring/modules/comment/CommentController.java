package com.spring.modules.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@RequestBody CommentRequest request) {
        CommentResponse response = commentService.createComment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByTicket(@PathVariable Long ticketId) {
        List<CommentResponse> responses = commentService.getCommentsByTicketId(ticketId);
        return ResponseEntity.ok(responses);
    }
}
