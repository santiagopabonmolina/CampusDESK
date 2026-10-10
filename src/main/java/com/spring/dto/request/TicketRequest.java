package com.spring.dto.request;

import com.spring.entity.enums.TicketCategory;
import com.spring.entity.enums.TicketPriority;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketRequest {

    private String title;
    private String description;
    private TicketCategory category;
    private TicketPriority priority;

    // ID del usuario solicitante
    private Long requesterId;

    // ID del técnico asignado (opcional al crear)
    private Long technicianId;
}