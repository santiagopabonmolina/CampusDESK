// Defines the package namespace containing JPA persistence entities
package com.spring.entity;

// Imports the RoleName enum specifying allowed security roles in CampusDESK
import com.spring.entity.enums.RoleName;
// Imports Jakarta Persistence API annotations for relational database mapping
import jakarta.persistence.*;
// Imports Lombok annotations to automatically generate boilerplate getter and setter code
import lombok.*;

// Marks this Java class as an active JPA database entity
@Entity
// Maps this entity to the relational database table named role
@Table(name = "role")
// Generates getter methods for all class fields at compilation time
@Getter
// Generates setter methods for all class fields at compilation time
@Setter
// Generates a default zero-argument constructor required by JPA providers
@NoArgsConstructor
// Generates a constructor accepting all entity fields as initialization arguments
@AllArgsConstructor
// Implements the builder pattern to construct Role instances with fluent methods
@Builder
// Declares the Role entity representing user permissions within the system
public class Role {

    // Specifies the primary key attribute for the role database table
    @Id
    // Configures auto-incrementing primary key generation using PostgreSQL identity columns
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Stores the unique database numeric identifier for this role
    private Long id;

    // Persists the enum value as a string representation rather than ordinal numbers
    @Enumerated(EnumType.STRING)
    // Defines column constraints requiring unique non-null values with max length twenty
    @Column(nullable = false, unique = true, length = 20)
    // Holds the specific role enumeration constant assigned to this entity
    private RoleName name;
// Closes the Role entity class definition
}
