package com.spring.service;

import com.spring.dto.request.CommentRequest;
import com.spring.dto.response.CommentResponse;
import com.spring.entity.Comment;
import com.spring.entity.Ticket;
import com.spring.entity.User;
import com.spring.entity.enums.TicketState;
import com.spring.repository.CommentRepository;
import com.spring.repository.TicketRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final EntityManager entityManager;

    @Transactional
    public CommentResponse createComment(CommentRequest request) {
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        if (ticket.getState() == TicketState.CLOSED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se permite agregar comentarios a un ticket cerrado");
        }

        User author = entityManager.getReference(User.class, request.getAuthorId());

        Comment comment = Comment.builder()
                .content(request.getContent())
                .author(author)
                .ticket(ticket)
                .build();

        Comment savedComment = commentRepository.save(comment);

        return mapToResponse(savedComment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTicketId(Long ticketId) {
        List<Comment> comments = commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        return comments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(comment.getAuthor().getId())
                .ticketId(comment.getTicket().getId())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
