package com.spring.service;

import com.spring.dto.request.CommentRequest;
import com.spring.dto.response.CommentResponse;
import com.spring.entity.Comment;
import com.spring.entity.Ticket;
import com.spring.entity.User;
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

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }

    private boolean hasRole(Authentication authentication, String roleName) {
        if (authentication == null || authentication.getAuthorities() == null) return false;
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (roleName.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private void checkTicketAccess(Ticket ticket, User currentUser, Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return;
        }
        if (hasRole(authentication, "ROLE_TECHNICIAN")) {
            if (ticket.getTechnicianAssigned() != null &&
                ticket.getTechnicianAssigned().getId().equals(currentUser.getId())) {
                return;
            }
        }
        if (ticket.getRequester() != null &&
            ticket.getRequester().getId().equals(currentUser.getId())) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver o comentar este ticket");
    }

    @Transactional
    public CommentResponse createComment(CommentRequest request, Authentication authentication) {
        User author = getAuthenticatedUser(authentication);
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket no encontrado"));

        checkTicketAccess(ticket, author, authentication);

        Comment comment = Comment.builder()
                .content(request.getContent())
                .author(author)
                .ticket(ticket)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return mapToResponse(savedComment);
    }

    @Transactional
    public CommentResponse createComment(CommentRequest request) {
        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket no encontrado"));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .author(author)
                .ticket(ticket)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return mapToResponse(savedComment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTicketId(Long ticketId, Authentication authentication) {
        User currentUser = getAuthenticatedUser(authentication);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket no encontrado"));

        checkTicketAccess(ticket, currentUser, authentication);

        List<Comment> comments = commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        return comments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTicketId(Long ticketId) {
        List<Comment> comments = commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        return comments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CommentResponse mapToResponse(Comment comment) {
        String authorName = null;
        String authorEmail = null;
        Long authorId = null;

        if (comment.getAuthor() != null) {
            authorId = comment.getAuthor().getId();
            String fn = comment.getAuthor().getFirstName() != null ? comment.getAuthor().getFirstName() : "";
            String ln = comment.getAuthor().getLastName() != null ? comment.getAuthor().getLastName() : "";
            authorName = (fn + " " + ln).trim();
            if (authorName.isEmpty()) {
                authorName = comment.getAuthor().getEmail();
            }
            authorEmail = comment.getAuthor().getEmail();
        }

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(authorId)
                .authorName(authorName)
                .authorEmail(authorEmail)
                .ticketId(comment.getTicket() != null ? comment.getTicket().getId() : null)
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
