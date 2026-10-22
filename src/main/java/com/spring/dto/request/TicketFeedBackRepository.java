package com.spring.dto.request;

import com.spring.entity.Ticket;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketFeedBackRepository {

    @NotNull(message = "La puntuacion es obligatorio")
    @Min(1)
    @Max(5)
    private int puntiation;

    private String comment;

    private Ticket ticket;
}
