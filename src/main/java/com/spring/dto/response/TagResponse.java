package com.spring.dto.response;

import com.spring.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagResponse {
    private Long id;
    private String nombre;

    public static TagResponse fromEntity(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .nombre(tag.getNombre())
                .build();

    }
}