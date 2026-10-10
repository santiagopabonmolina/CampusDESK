package com.spring.dto.request;

import lombok.Data;

@Data
public class CommentRequest {
    private String content;
    private Long ticketId;
    private Long authorId;
}