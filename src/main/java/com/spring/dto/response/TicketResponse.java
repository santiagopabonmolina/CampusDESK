// Defines the package namespace containing outbound server response DTOs
package com.spring.dto.response;

// Imports the Ticket entity converted into this response transfer object
import com.spring.entity.Ticket;
// Imports TicketCategory enum for displaying ticket category classification
import com.spring.entity.enums.TicketCategory;
// Imports TicketPriority enum for displaying ticket priority urgency level
import com.spring.entity.enums.TicketPriority;
// Imports TicketState enum for displaying ticket current workflow lifecycle state
import com.spring.entity.enums.TicketState;
// Imports Lombok annotations for automatic constructor, getter, setter, and builder generation
import lombok.*;

// Imports Java time class for storing ticket dates and timestamps
import java.time.LocalDateTime;

// Generates getter accessor methods for all ticket response attributes
@Getter
// Generates setter mutator methods for all ticket response attributes
@Setter
// Generates zero-argument default constructor required for JSON serialization
@NoArgsConstructor
// Generates constructor with parameters for all declared response attributes
@AllArgsConstructor
// Implements the builder pattern for constructing ticket responses fluently
@Builder
// Declares data transfer object carrying detailed ticket information to frontend
public class TicketResponse {

    // Stores the unique database numeric identifier of the ticket
    private Long id;
    // Stores the concise headline title of the reported ticket
    private String title;
    // Stores the detailed textual description of the technical issue
    private String description;
    // Stores the categorized incident domain classification of the ticket
    private TicketCategory category;
    // Stores the urgency priority rating assigned to this ticket
    private TicketPriority priority;
    // Stores the current workflow lifecycle status of this ticket
    private TicketState state;

    // Stores the unique database identifier of the requesting user
    private Long requesterId;
    // Stores the concatenated first and last name of requester
    private String requesterName;
    // Stores the institutional email address of the requesting user
    private String requesterEmail;

    // Stores the database identifier of the assigned technical technician
    private Long technicianId;
    // Stores the concatenated first and last name of technician
    private String technicianName;

    // Stores the date and timestamp when ticket was created
    private LocalDateTime createDate;
    // Stores the timestamp when technician resolved this support ticket
    private LocalDateTime resolutionDate;
    // Stores the timestamp when ticket was formally marked closed
    private LocalDateTime closeDate;

    /**
     * Maps entity to response.
     */
    // Converts a JPA Ticket entity into an outbound TicketResponse transfer object
    public static TicketResponse fromEntity(Ticket ticket) {
        // Returns null immediately if the supplied ticket entity is null
        if (ticket == null) return null;

        // Initializes variable to hold formatted requester full name
        String reqName = null;
        // Initializes variable to hold requester contact email address
        String reqEmail = null;
        // Checks whether the ticket contains an associated requester user
        if (ticket.getRequester() != null) {
            // Concatenates requester first and last name with a separating space
            reqName = ticket.getRequester().getFirstName() + " " + ticket.getRequester().getLastName();
            // Extracts email address from the associated requester user entity
            reqEmail = ticket.getRequester().getEmail();
        // Closes if condition block
        }

        // Initializes variable to hold formatted technician full name
        String techName = null;
        // Initializes variable to hold technician assigned identifier
        Long techId = null;
        // Checks whether a technician is currently assigned to this ticket
        if (ticket.getTechnicianAssigned() != null) {
            // Extracts numeric identifier from assigned technician user entity
            techId = ticket.getTechnicianAssigned().getId();
            // Concatenates technician first and last name with separating space
            techName = ticket.getTechnicianAssigned().getFirstName() + " " + ticket.getTechnicianAssigned().getLastName();
        // Closes if condition block
        }

        // Builds and returns the TicketResponse populating all fields from ticket entity and helper variables
        return TicketResponse.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .category(ticket.getCategory())
                .priority(ticket.getPriority())
                .state(ticket.getState())
                .requesterId(ticket.getRequester() != null ? ticket.getRequester().getId() : null)
                .requesterName(reqName)
                .requesterEmail(reqEmail)
                .technicianId(techId)
                .technicianName(techName)
                .createDate(ticket.getCreateDate())
                .resolutionDate(ticket.getResolutionDate())
                .closeDate(ticket.getCloseDate())
                .build();
    // Closes fromEntity static mapping method
    }
// Closes TicketResponse class definition
}
