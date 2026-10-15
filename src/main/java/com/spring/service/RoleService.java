// Defines the package namespace containing business service layer classes
package com.spring.service;

// Imports RoleRequest DTO containing payload data for role operations
import com.spring.dto.request.RoleRequest;
// Imports RoleResponse DTO transferring role entity data to clients
import com.spring.dto.response.RoleResponse;
// Imports Role JPA entity representing role database records
import com.spring.entity.Role;
// Imports RoleName enum representing supported user security roles
import com.spring.entity.enums.RoleName;
// Imports RoleRepository interface providing database operations for roles
import com.spring.repository.RoleRepository;
// Imports Lombok annotation generating constructor for final dependency fields
import lombok.RequiredArgsConstructor;
// Imports Spring Service annotation declaring this class as business service component
import org.springframework.stereotype.Service;
// Imports Spring Transactional annotation for managing transactional database boundaries
import org.springframework.transaction.annotation.Transactional;

// Imports Java utility List collection interface for returning sequential collections
import java.util.List;
// Imports Collectors utility class for accumulating stream elements into collections
import java.util.stream.Collectors;

// Marks this class as Spring service component for role management operations
@Service
// Generates constructor injecting required final repository dependencies automatically
@RequiredArgsConstructor
// Declares RoleService class managing business logic for system authorization roles
public class RoleService {

    // Declares final repository reference for executing role database persistence operations
    private final RoleRepository roleRepository;

    /**
     * Creates and persists role.
     */
    // Manages database transaction boundary ensuring atomic role insertion operations
    @Transactional
    // Creates a new system role validating its unique existence in database
    public RoleResponse createRole(RoleRequest request) {
        // Converts request name string to uppercase RoleName enum representation
        RoleName roleName = RoleName.valueOf(request.getName().toUpperCase());

        // Checks whether role with matching name already exists in database
        if (roleRepository.findByName(roleName).isPresent()) {
            // Throws IllegalArgumentException preventing creation of duplicate role names
            throw new IllegalArgumentException("Role already exists: " + roleName);
        // Closes if condition checking role existence
        }

        // Builds Role entity instance initializing the name enum property
        Role role = Role.builder()
                .name(roleName)
                .build();

        // Persists new Role entity to relational database table
        Role savedRole = roleRepository.save(role);

        // Converts persisted role entity into response DTO and returns it
        return mapToResponse(savedRole);
    // Closes createRole method
    }

    /**
     * Retrieves all persisted roles.
     */
    // Configures read-only transaction optimizing performance for querying all roles
    @Transactional(readOnly = true)
    // Retrieves list of all configured roles currently available in system
    public List<RoleResponse> getAllRoles() {
        // Queries all roles from database streaming and mapping each to response
        return roleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    // Closes getAllRoles method
    }

    /**
     * Maps role to response.
     */
    // Private helper method converting Role entity into RoleResponse transfer object
    private RoleResponse mapToResponse(Role role) {
        // Builds and returns RoleResponse populating id and name from role
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .build();
    // Closes mapToResponse helper method
    }
// Closes RoleService class definition
}
