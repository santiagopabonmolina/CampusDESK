// Defines the package namespace containing outbound server response DTOs
package com.spring.dto.response;

// Imports Lombok annotation generating constructor taking all class field arguments
import lombok.AllArgsConstructor;
// Imports Lombok annotation implementing the fluent builder creation pattern
import lombok.Builder;
// Imports Lombok annotation generating getters, setters, toString, equals, and hashcode
import lombok.Data;
// Imports Lombok annotation generating zero-argument default public constructor
import lombok.NoArgsConstructor;

// Generates boilerplate data access methods across all authentication response attributes
@Data
// Implements the builder pattern for constructing authentication token responses fluently
@Builder
// Generates empty default constructor required by serialization and deserialization
@NoArgsConstructor
// Generates constructor accepting values for all declared token response fields
@AllArgsConstructor
// Declares data transfer object carrying JWT token and authenticated user metadata
public class JwtResponse {
    // Stores the signed JSON Web Token string returned to client
    private String token;
    // Instructs Lombok builder to preserve default schema prefix value
    @Builder.Default
    // Stores the HTTP Authorization token prefix type standard Bearer
    private String type = "Bearer";
    // Stores the unique database identifier of the authenticated user
    private Long userId;
    // Stores the email address of the authenticated user account
    private String email;
    // Stores the given first name of the authenticated user
    private String firstName;
    // Stores the surname or family name of authenticated user
    private String lastName;
    // Stores the primary authorization role assigned to this user
    private String role;
// Closes the JwtResponse class definition
}
