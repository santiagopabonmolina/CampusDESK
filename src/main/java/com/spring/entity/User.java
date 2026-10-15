// Defines the package namespace containing JPA persistence entities
package com.spring.entity;

// Imports Jackson annotation for fine-grained JSON serialization property control
import com.fasterxml.jackson.annotation.JsonProperty;
// Imports Jakarta Persistence API annotations for relational database mapping
import jakarta.persistence.*;
// Imports Lombok annotations to generate boilerplate getters, setters, and builders
import lombok.*;

// Imports Java time class representing calendar date and local time
import java.time.LocalDateTime;
// Imports HashSet collection implementation for storing unique role elements
import java.util.HashSet;
// Imports Set interface representing an unordered collection of unique elements
import java.util.Set;

// Declares this class as a persistent entity mapped to database
@Entity
// Maps this entity to the relational database table named users
@Table(name = "users")
// Generates getter accessor methods for all class fields at compilation
@Getter
// Generates setter mutator methods for all class fields at compilation
@Setter
// Generates zero-argument default constructor required by the JPA specification
@NoArgsConstructor
// Generates constructor taking arguments for all fields declared in class
@AllArgsConstructor
// Implements the builder design pattern for flexible object construction
@Builder
// Declares the User entity representing system accounts in CampusDESK
public class User {

    // Marks this field as the primary key of the entity
    @Id
    // Configures auto-incrementing identity column strategy supported by PostgreSQL
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Stores the unique database numeric identifier for this user
    private Long id;

    // Configures required non-nullable varchar column with maximum length 150
    @Column(nullable = false, length = 150)
    // Stores the first given name of the campus user
    private String firstName;

    // Configures required non-nullable varchar column with maximum length 150
    @Column(nullable = false, length = 150)
    // Stores the surname or family name of the campus user
    private String lastName;

    // Enforces unique non-nullable constraint on email column with length 150
    @Column(nullable = false, unique = true, length = 150)
    // Stores the institutional email address utilized for system login
    private String email;

    // Ensures the password is only deserialized and never exposed in responses
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    // Configures required non-nullable varchar column storing the hashed password
    @Column(nullable = false, length = 255)
    // Stores the BCrypt encrypted password hash for authentication
    private String password;

    // Instructs Lombok builder to retain default field initialization value
    @Builder.Default
    // Configures required non-nullable boolean column in the database
    @Column(nullable = false)
    // Indicates whether the user account is active or soft-deleted
    private Boolean active = true;

    // Maps registration timestamp column and prevents modification after insertion
    @Column(name = "register_date", updatable = false)
    // Stores the exact timestamp when the user account was created
    private LocalDateTime registerDate;

    // Instructs Lombok builder to initialize the roles set with default HashSet
    @Builder.Default
    // Maps many-to-many relationship with eager fetching for user authorities
    @ManyToMany(fetch = FetchType.EAGER)
    // Specifies intermediary join table user_roles linking user and role keys
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    // Stores the set of authorized security roles assigned to user
    private Set<Role> roles = new HashSet<>();

    /**
     * Sets defaults before persist.
     */
    // Registers lifecycle callback invoked before entity record is persisted
    @PrePersist
    // Initializes default registration timestamp and active state before persisting
    protected void onCreate() {
        // Sets registration timestamp to the current local date and time
        this.registerDate = LocalDateTime.now();
        // Evaluates whether active status was left null prior to persisting
        if (this.active == null) {
            // Assigns true as the default state for active flag
            this.active = true;
        // Closes if condition block
        }
    // Closes onCreate callback method
    }
// Closes User class definition
}
