package com.spring.service;

import com.spring.dto.request.RoleRequest;
import com.spring.dto.response.RoleResponse;
import com.spring.entity.Role;
import com.spring.entity.enums.RoleName;
import com.spring.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    /**
     * Creates and persists role.
     */
    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        RoleName roleName = RoleName.valueOf(request.getName().toUpperCase());

        if (roleRepository.findByName(roleName).isPresent()) {
            throw new IllegalArgumentException("Role already exists: " + roleName);
        }

        Role role = Role.builder()
                .name(roleName)
                .build();

        Role savedRole = roleRepository.save(role);

        return mapToResponse(savedRole);
    }

    /**
     * Retrieves all persisted roles.
     */
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Maps role to response.
     */
    private RoleResponse mapToResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .build();
    }
}
