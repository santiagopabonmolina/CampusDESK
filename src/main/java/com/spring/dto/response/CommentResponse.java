// Defines the package namespace containing outbound server response DTOs
package com.spring.dto.response;

// Imports Lombok builder annotation providing fluent instance creation methods
import lombok.Builder;
// Imports Lombok data annotation bundling getters, setters, equals, and hashcode
import lombok.Data;

// Imports Java time class for storing date and time timestamps
import java.time.LocalDateTime;

// Generates boilerplate data access methods across all response attributes
@Data
// Implements the builder pattern for constructing response objects fluently
@Builder
// Declares data transfer object carrying comment data back to frontend
public class CommentResponse {
    // Holds the unique database primary key of the comment
    private Long id;
    // Holds the text body content of the comment message
    private String content;
    // Holds the unique identifier of the user who commented
    private Long authorId;
    // Holds the formatted full name of the comment author
    private String authorName;
    // Holds the identifier of the ticket containing this comment
    private Long ticketId;
    // Holds the timestamp when this comment was initially submitted
    private LocalDateTime createdAt;
// Closes the CommentResponse class definition
}
