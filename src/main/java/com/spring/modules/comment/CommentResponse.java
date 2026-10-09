package com.spring.modules.comment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private String content;
    private Long authorId;
    private Long ticketId;
    private LocalDateTime createdAt;
}
