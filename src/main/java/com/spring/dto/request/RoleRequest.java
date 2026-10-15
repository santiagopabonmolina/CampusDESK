// Defines the package namespace containing inbound client request DTOs
package com.spring.dto.request;

// Imports Lombok annotation generating getters, setters, toString, equals, and hashCode
import lombok.Data;

// Generates boilerplate data access methods across all request fields
@Data
// Declares data transfer object carrying role creation or assignment requests
public class RoleRequest {
    // Holds the string name representation of the role to assign
    private String name;
// Closes the RoleRequest class definition
}
