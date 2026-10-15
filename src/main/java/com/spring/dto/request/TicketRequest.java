// Defines the package namespace containing inbound client request DTOs
package com.spring.dto.request;

// Imports TicketCategory enum specifying incident domain classification categories
import com.spring.entity.enums.TicketCategory;
// Imports TicketPriority enum specifying incident urgency priority levels
import com.spring.entity.enums.TicketPriority;
// Imports Jakarta validation annotation preventing blank or empty strings
import jakarta.validation.constraints.NotBlank;
// Imports Jakarta validation annotation verifying required non-null object values
import jakarta.validation.constraints.NotNull;
// Imports Jakarta validation annotation restricting maximum string character length
import jakarta.validation.constraints.Size;
// Imports Lombok annotations for automatic constructor and accessor generation
import lombok.*;

// Generates getter accessor methods for all declared request fields
@Getter
// Generates setter mutator methods for all declared request fields
@Setter
// Generates zero-argument constructor required for JSON payload deserialization
@NoArgsConstructor
// Generates parameterized constructor accepting values for all declared fields
@AllArgsConstructor
// Implements the builder pattern for constructing request instances fluently
@Builder
// Declares data transfer object representing ticket creation and modification payload
public class TicketRequest {

    // Validates that the incident title is not blank
    @NotBlank(message = "El título es obligatorio")
    // Restricts maximum allowed length of title to 225 characters
    @Size(max = 225, message = "El título no debe superar los 225 caracteres")
    // Stores the brief summary headline describing the reported issue
    private String title;

    // Validates that the incident description is not blank
    @NotBlank(message = "La descripción es obligatoria")
    // Stores the comprehensive narrative detailing the technical problem encountered
    private String description;

    // Validates that an incident category enum value is provided
    @NotNull(message = "La categoría es obligatoria")
    // Holds the technical category classification chosen for this ticket
    private TicketCategory category;

    // Validates that an urgency priority enum value is provided
    @NotNull(message = "La prioridad es obligatoria")
    // Holds the urgency level assigned to this technical incident
    private TicketPriority priority;

    // ID del usuario solicitante
    // Validates that the requester user identifier is provided
    @NotNull(message = "El solicitante es obligatorio")
    // Stores the database identifier of the user creating ticket
    private Long requesterId;

    // ID del técnico asignado (opcional al crear)
    // Stores the database identifier of the assigned technical specialist
    private Long technicianId;
// Closes the TicketRequest class definition
}
