package com.spring.dto.request;

import com.spring.entity.enums.TicketCategory;
import com.spring.entity.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 225, message = "El título no debe superar los 225 caracteres")
    private String title;

    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    @NotNull(message = "La categoría es obligatoria")
    private TicketCategory category;

    @NotNull(message = "La prioridad es obligatoria")
    private TicketPriority priority;

    // ID del usuario solicitante
    @NotNull(message = "El solicitante es obligatorio")
    private Long requesterId;

    // ID del técnico asignado (opcional al crear)
    private Long technicianId;
}
