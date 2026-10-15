// Defines the package namespace containing business service layer classes
package com.spring.service;

// Imports CommentRequest DTO containing client-submitted comment payload data
import com.spring.dto.request.CommentRequest;
// Imports CommentResponse DTO transferring comment data to presentation layer
import com.spring.dto.response.CommentResponse;
// Imports Comment JPA entity representing comment database records
import com.spring.entity.Comment;
// Imports Ticket JPA entity representing support ticket database records
import com.spring.entity.Ticket;
// Imports User JPA entity representing user account records
import com.spring.entity.User;
// Imports TicketState enum for validating ticket lifecycle state conditions
import com.spring.entity.enums.TicketState;
// Imports CommentRepository interface providing database operations for comments
import com.spring.repository.CommentRepository;
// Imports TicketRepository interface providing database operations for tickets
import com.spring.repository.TicketRepository;
// Imports UserRepository interface providing database operations for users
import com.spring.repository.UserRepository;
// Imports Lombok annotation generating constructor for final dependency fields
import lombok.RequiredArgsConstructor;
// Imports HttpStatus enum representing standard HTTP response status codes
import org.springframework.http.HttpStatus;
// Imports Spring Security Authentication representing the authenticated user principal
import org.springframework.security.core.Authentication;
// Imports GrantedAuthority interface representing security authorities and roles
import org.springframework.security.core.GrantedAuthority;
// Imports Spring Service annotation declaring this class as business service component
import org.springframework.stereotype.Service;
// Imports Spring Transactional annotation for managing transactional database boundaries
import org.springframework.transaction.annotation.Transactional;
// Imports ResponseStatusException for throwing HTTP-aware exceptions from services
import org.springframework.web.server.ResponseStatusException;

// Imports Java utility List collection interface for handling sequential lists
import java.util.List;
// Imports Collectors utility class for transforming collections via streams
import java.util.stream.Collectors;

// Marks this class as Spring service component for business logic execution
@Service
// Generates constructor injecting all required final repository dependencies automatically
@RequiredArgsConstructor
// Declares CommentService class handling business logic for ticket comments
public class CommentService {

    // Declares final repository reference for executing comment database operations
    private final CommentRepository commentRepository;
    // Declares final repository reference for verifying and retrieving ticket entities
    private final TicketRepository ticketRepository;
    // Declares final repository reference for finding and verifying author user entities
    private final UserRepository userRepository;

    /**
     * Creates authenticated ticket comment.
     */
    // Manages database transaction boundary ensuring atomic comment insertion operations
    @Transactional
    // Creates a new comment validating ticket existence, status, and permissions
    public CommentResponse createComment(CommentRequest request, Authentication authentication) {
        // Retrieves target ticket by ID or throws HTTP 404 if not found
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        // Checks whether target ticket has already reached closed lifecycle state
        if (ticket.getState() == TicketState.CLOSED) {
            // Throws HTTP 409 conflict exception preventing commenting on closed tickets
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se permite agregar comentarios a un ticket cerrado");
        // Closes if condition checking closed ticket state
        }

        // Checks whether active authentication context was provided with request
        if (authentication != null) {
            // Retrieves email username of currently authenticated principal user
            String email = authentication.getName();
            // Evaluates whether current user possesses administrator authorization role
            boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");

            // Executes authorization checks for non-administrator authenticated campus users
            if (!isAdmin) {
                // Evaluates whether current user possesses technician authorization role
                boolean isTechnician = hasRole(authentication, "ROLE_TECHNICIAN");

                // Verifies technician assignment permissions when user is support technician
                if (isTechnician) {
                    // Verifies that technician is assigned to this ticket before commenting
                    if (ticket.getTechnicianAssigned() == null ||
                            !email.equalsIgnoreCase(ticket.getTechnicianAssigned().getEmail())) {
                        // Throws HTTP 403 forbidden exception if technician is not assigned
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "Solo puedes comentar en tickets que tienes asignados");
                    // Closes if condition checking technician assignment
                    }
                // Executes requester permission check when user is standard requester
                } else {
                    // Verifies that requester created this ticket before allowing comments
                    if (ticket.getRequester() == null ||
                            !email.equalsIgnoreCase(ticket.getRequester().getEmail())) {
                        // Throws HTTP 403 forbidden exception if user is not ticket owner
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "Solo puedes comentar en tus propias solicitudes");
                    // Closes if condition checking requester ownership
                    }
                // Closes else block for standard user authorization
                }
            // Closes if condition block for non-admin users
            }
        // Closes if condition block checking authentication presence
        }

        // Retrieves author user entity by authorId or throws HTTP 404
        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Autor no encontrado"));

        // Builds Comment entity populating content, author, and associated ticket
        Comment comment = Comment.builder()
                .content(request.getContent())
                .author(author)
                .ticket(ticket)
                .build();

        // Persists new Comment entity instance to PostgreSQL database table
        Comment savedComment = commentRepository.save(comment);

        // Converts saved entity to response DTO and returns it
        return mapToResponse(savedComment);
    // Closes createComment method
    }

    /**
     * Creates ticket comment overload.
     */
    // Manages transactional database boundary for overloaded comment creation method
    @Transactional
    // Overloaded method creating comment without passing explicit authentication context
    public CommentResponse createComment(CommentRequest request) {
        // Delegates execution to main createComment method passing null authentication
        return createComment(request, null);
    // Closes overloaded createComment method
    }

    /**
     * Retrieves ticket comment list.
     */
    // Executes read-only transaction optimizing performance for querying ticket comments
    @Transactional(readOnly = true)
    // Retrieves chronologically sorted list of comments for specified ticket ID
    public List<CommentResponse> getCommentsByTicketId(Long ticketId) {
        // Queries database for comments belonging to ticket ordered by creation date
        List<Comment> comments = commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        // Streams comments collection mapping each entity to outbound response DTO
        return comments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    // Closes getCommentsByTicketId method
    }

    /**
     * Maps comment to response.
     */
    // Private helper method mapping Comment database entity to CommentResponse DTO
    private CommentResponse mapToResponse(Comment comment) {
        // Constructs CommentResponse object populating author name and timestamps
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(comment.getAuthor().getId())
                .authorName(comment.getAuthor().getFirstName() + " " + comment.getAuthor().getLastName())
                .ticketId(comment.getTicket().getId())
                .createdAt(comment.getCreatedAt())
                .build();
    // Closes mapToResponse helper method
    }

    /**
     * Checks granted authority role.
     */
    // Private helper evaluating whether Authentication contains a specific granted authority
    private boolean hasRole(Authentication authentication, String role) {
        // Iterates through granted authorities associated with current authenticated user
        for (GrantedAuthority ga : authentication.getAuthorities()) {
            // Compares granted authority string with target role name argument
            if (role.equals(ga.getAuthority())) {
                // Returns true immediately upon finding matching security role authority
                return true;
            // Closes if condition block
            }
        // Closes for loop iterating authorities
        }
        // Returns false when matching security authority is not found
        return false;
    // Closes hasRole helper method
    }
// Closes CommentService class definition
}
