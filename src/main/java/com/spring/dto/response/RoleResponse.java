package com.spring.dto.response;

import com.spring.entity.enums.RoleName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleResponse {
    private Long id;
    private RoleName name;
}