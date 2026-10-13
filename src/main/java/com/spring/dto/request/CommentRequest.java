package com.spring.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequest {

    @NotBlank(message = "El contenido del comentario es obligatorio")
    private String content;

    private Long ticketId;

    @NotNull(message = "El autor es obligatorio")
    private Long authorId;
}
