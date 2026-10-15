package com.spring.dto.response;

import com.spring.entity.Ticket;
import com.spring.entity.enums.TicketCategory;
import com.spring.entity.enums.TicketPriority;
import com.spring.entity.enums.TicketState;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {

    private Long id;
    private String title;
    private String description;
    private TicketCategory category;
    private TicketPriority priority;
    private TicketState state;

    private Long requesterId;
    private String requesterName;
    private String requesterEmail;

    private Long technicianId;
    private String technicianName;

    private LocalDateTime createDate;
    private LocalDateTime resolutionDate;
    private LocalDateTime closeDate;

    /**
     * Maps entity to response.
     */
    public static TicketResponse fromEntity(Ticket ticket) {
        if (ticket == null) return null;

        String reqName = null;
        String reqEmail = null;
        if (ticket.getRequester() != null) {
            reqName = ticket.getRequester().getFirstName() + " " + ticket.getRequester().getLastName();
            reqEmail = ticket.getRequester().getEmail();
        }

        String techName = null;
        Long techId = null;
        if (ticket.getTechnicianAssigned() != null) {
            techId = ticket.getTechnicianAssigned().getId();
            techName = ticket.getTechnicianAssigned().getFirstName() + " " + ticket.getTechnicianAssigned().getLastName();
        }

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
    }
}
