// Defines the package namespace containing inbound client request DTOs
package com.spring.dto.request;

// Imports Jakarta validation annotation verifying valid electronic mail syntax format
import jakarta.validation.constraints.Email;
// Imports Jakarta validation annotation preventing null or empty string input
import jakarta.validation.constraints.NotBlank;
// Imports Lombok annotation generating a constructor accepting all class fields
import lombok.AllArgsConstructor;
// Imports Lombok builder annotation providing fluent instance creation pattern
import lombok.Builder;
// Imports Lombok annotation generating getters, setters, toString, and equals methods
import lombok.Data;
// Imports Lombok annotation generating a zero-argument default constructor
import lombok.NoArgsConstructor;

// Generates getters, setters, equals, and string representations automatically
@Data
// Implements the builder pattern for constructing request instances fluently
@Builder
// Generates default no-argument constructor for JSON payload deserialization
@NoArgsConstructor
// Generates constructor with parameters for all declared class attributes
@AllArgsConstructor
// Declares data transfer object representing user authentication credentials payload
public class LoginRequest {
    // Validates that email string cannot be left blank by client
    @NotBlank(message = "El correo electronico es obligatorio")
    // Validates that string adheres to standard email address format
    @Email(message = "El formato del correo no es valido")
    // Holds the institutional user email address provided for login
    private String email;

    // Validates that the password field cannot be left blank
    @NotBlank(message = "La contraseño es obligatorio")
    // Holds the plain-text password submitted for authentication verification
    private String password;
// Closes the LoginRequest class definition
}
