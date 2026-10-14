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

    // Datos aplanados del solicitante
    private Long requesterId;
    private String requesterName;
    private String requesterEmail;

    // Datos aplanados del técnico (si aplica)
    private Long technicianId;
    private String technicianName;

    private LocalDateTime createDate;
    private LocalDateTime resolutionDate;
    private LocalDateTime closeDate;

    // Mapeo seguro Entity -> DTO evitando recursión y proxies sin inicializar
    public static TicketResponse fromEntity(Ticket ticket) {
        if (ticket == null) return null;

        String reqName = null;
        String reqEmail = null;
        Long reqId = null;
        if (ticket.getRequester() != null) {
            reqId = ticket.getRequester().getId();
            String fn = ticket.getRequester().getFirstName() != null ? ticket.getRequester().getFirstName() : "";
            String ln = ticket.getRequester().getLastName() != null ? ticket.getRequester().getLastName() : "";
            reqName = (fn + " " + ln).trim();
            if (reqName.isEmpty()) {
                reqName = ticket.getRequester().getEmail();
            }
            reqEmail = ticket.getRequester().getEmail();
        }

        String techName = null;
        Long techId = null;
        if (ticket.getTechnicianAssigned() != null) {
            techId = ticket.getTechnicianAssigned().getId();
            String fn = ticket.getTechnicianAssigned().getFirstName() != null ? ticket.getTechnicianAssigned().getFirstName() : "";
            String ln = ticket.getTechnicianAssigned().getLastName() != null ? ticket.getTechnicianAssigned().getLastName() : "";
            techName = (fn + " " + ln).trim();
            if (techName.isEmpty()) {
                techName = ticket.getTechnicianAssigned().getEmail();
            }
        }

        return TicketResponse.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .category(ticket.getCategory())
                .priority(ticket.getPriority())
                .state(ticket.getState())
                .requesterId(reqId)
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
