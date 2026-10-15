package com.spring.service;

import com.spring.dto.request.CommentRequest;
import com.spring.dto.response.CommentResponse;
import com.spring.entity.Comment;
import com.spring.entity.Ticket;
import com.spring.entity.User;
import com.spring.entity.enums.TicketState;
import com.spring.repository.CommentRepository;
import com.spring.repository.TicketRepository;
import com.spring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
    private final UserRepository userRepository;

    /**
     * Creates authenticated ticket comment.
     */
    @Transactional
    public CommentResponse createComment(CommentRequest request, Authentication authentication) {
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        if (ticket.getState() == TicketState.CLOSED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se permite agregar comentarios a un ticket cerrado");
        }

        if (authentication != null) {
            String email = authentication.getName();
            boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");

            if (!isAdmin) {
                boolean isTechnician = hasRole(authentication, "ROLE_TECHNICIAN");

                if (isTechnician) {
                    if (ticket.getTechnicianAssigned() == null ||
                            !email.equalsIgnoreCase(ticket.getTechnicianAssigned().getEmail())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "Solo puedes comentar en tickets que tienes asignados");
                    }
                } else {
                    if (ticket.getRequester() == null ||
                            !email.equalsIgnoreCase(ticket.getRequester().getEmail())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "Solo puedes comentar en tus propias solicitudes");
                    }
                }
            }
        }

        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Autor no encontrado"));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .author(author)
                .ticket(ticket)
                .build();

        Comment savedComment = commentRepository.save(comment);

        return mapToResponse(savedComment);
    }

    /**
     * Creates ticket comment overload.
     */
    @Transactional
    public CommentResponse createComment(CommentRequest request) {
        return createComment(request, null);
    }

    /**
     * Retrieves ticket comment list.
     */
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTicketId(Long ticketId) {
        List<Comment> comments = commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        return comments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Maps comment to response.
     */
    private CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(comment.getAuthor().getId())
                .authorName(comment.getAuthor().getFirstName() + " " + comment.getAuthor().getLastName())
                .ticketId(comment.getTicket().getId())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    /**
     * Checks granted authority role.
     */
    private boolean hasRole(Authentication authentication, String role) {
        for (GrantedAuthority ga : authentication.getAuthorities()) {
            if (role.equals(ga.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
