// Defines the package namespace containing inbound client request DTOs
package com.spring.dto.request;

// Imports Jakarta validation annotation verifying valid email address formatting
import jakarta.validation.constraints.Email;
// Imports Jakarta validation annotation rejecting null or whitespace-only strings
import jakarta.validation.constraints.NotBlank;
// Imports Jakarta validation annotation enforcing string minimum and maximum lengths
import jakarta.validation.constraints.Size;
// Imports Lombok annotation generating constructor with all class fields
import lombok.AllArgsConstructor;
// Imports Lombok annotation implementing the builder pattern for instances
import lombok.Builder;
// Imports Lombok annotation generating getters, setters, equals, and hashcode
import lombok.Data;
// Imports Lombok annotation generating zero-argument default public constructor
import lombok.NoArgsConstructor;

// Generates comprehensive data accessor and utility methods for class
@Data
// Provides fluent API for building RegisterRequest instances programmatically
@Builder
// Generates empty default constructor required by Jackson for deserialization
@NoArgsConstructor
// Generates constructor accepting values for all four user attributes
@AllArgsConstructor
// Declares data transfer object representing new account registration payload
public class RegisterRequest {
    // Validates that first name input is not blank
    @NotBlank(message = "El nombre es obligatorio ")
    // Restricts maximum allowed length of first name to 150 characters
    @Size(max = 150, message = "El nombre no debe superar los 150 caracteres")
    // Holds the first name of the user being registered
    private String firstName;

    // Validates that last name input is not blank
    @NotBlank(message = "El apellido es obligatorio ")
    // Restricts maximum allowed length of last name to 150 characters
    @Size(max = 150, message = "El apellido no debe superar los 150 caracteres")
    // Holds the family name of the user being registered
    private String lastName;

    // Validates that email string input is not blank
    @NotBlank(message = "El correo es obligatorio ")
    // Enforces valid email syntax according to standard web formats
    @Email(message = "El formato del correo no es valido")
    // Restricts maximum allowed length of email to 150 characters
    @Size(max = 150, message = "El correo no debe superar los 150 caracteres")
    // Holds the unique contact email for the new user
    private String email;

    // Validates that password string input is not blank
    @NotBlank(message = "La contraseña es obligatoria")
    // Enforces password security length bounds between six and 150 characters
    @Size(min = 6, max = 150, message = "La contraseña debe tener entre 6 y 150 caracteres")
    // Holds the plain-text password chosen during user account registration
    private String password;
// Closes the RegisterRequest class definition
}
