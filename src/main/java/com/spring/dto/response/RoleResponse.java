// Defines the package namespace containing outbound server response DTOs
package com.spring.dto.response;

// Imports the RoleName enum representing system authorization security roles
import com.spring.entity.enums.RoleName;
// Imports Lombok annotation providing fluent builder pattern instantiation methods
import lombok.Builder;
// Imports Lombok annotation generating getters, setters, toString, equals, and hashcode
import lombok.Data;

// Generates boilerplate data access methods across all role response attributes
@Data
// Implements the builder pattern for constructing role response objects fluently
@Builder
// Declares data transfer object carrying role entity information to frontend
public class RoleResponse {
    // Stores the unique database numeric identifier of the role
    private Long id;
    // Stores the enumeration constant specifying the authority role name
    private RoleName name;
// Closes the RoleResponse class definition
}
