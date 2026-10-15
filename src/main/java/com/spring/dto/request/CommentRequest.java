// Defines the package namespace containing inbound client request DTOs
package com.spring.dto.request;

// Imports Jakarta validation annotation requiring non-empty string input data
import jakarta.validation.constraints.NotBlank;
// Imports Jakarta validation annotation rejecting null values for required fields
import jakarta.validation.constraints.NotNull;
// Imports Lombok annotation generating getters, setters, equals, hashCode, and toString
import lombok.Data;

// Bundles standard boilerplate data methods across all request fields
@Data
// Declares data transfer object carrying comment creation payload from clients
public class CommentRequest {

    // Validates that comment text cannot be null or blank whitespace
    @NotBlank(message = "El contenido del comentario es obligatorio")
    // Holds the message body content of the submitted comment
    private String content;

    // Stores the target ticket ID to which this comment belongs
    private Long ticketId;

    // Validates that the author user ID cannot be null
    @NotNull(message = "El autor es obligatorio")
    // Holds the unique identifier of the user submitting comment
    private Long authorId;
// Closes the CommentRequest class definition
}
