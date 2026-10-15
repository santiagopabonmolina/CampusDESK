// Defines the package namespace containing JPA persistence entities
package com.spring.entity;



// Imports Jakarta Persistence API annotations for object-relational mapping
import jakarta.persistence.*;
// Imports Lombok annotations to automatically eliminate boilerplate accessor code
import lombok.*;
// Imports Hibernate annotation for automatically populating creation timestamps
import org.hibernate.annotations.CreationTimestamp;

// Imports Java time class representing date-time without timezone offset
import java.time.LocalDateTime;

// Marks this Java class as a persistent entity mapped to database tables
@Entity
// Maps this entity to the relational database table named comment
@Table(name = "comment")
// Generates getter accessor methods for all class fields at compile time
@Getter
// Generates setter mutator methods for all class fields at compile time
@Setter
// Generates a no-argument constructor required by the JPA persistence provider
@NoArgsConstructor
// Generates a constructor accepting arguments for all declared entity fields
@AllArgsConstructor
// Generates a fluent builder pattern implementation for creating Comment instances
@Builder
// Declares the Comment entity representing feedback or updates on tickets
public class Comment {

    // Specifies the primary key attribute for the comment table
    @Id
    // Automatically generates unique sequential IDs using PostgreSQL identity columns
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Stores the unique database numeric identifier for this comment
    private Long id;

    // Configures database column as non-nullable text storing extensive notes
    @Column(nullable = false, columnDefinition = "TEXT")
    // Holds the text message or explanation submitted by the user
    private String content;

    // Establishes a many-to-one relationship using lazy loading for author
    @ManyToOne(fetch = FetchType.LAZY)
    // Maps foreign key column author_id referencing the users table
    @JoinColumn(name = "author_id", nullable = false)
    // Holds the user entity reference who authored this comment
    private User author;

    // Establishes a many-to-one relationship using lazy loading for ticket
    @ManyToOne(fetch = FetchType.LAZY)
    // Maps foreign key column ticket_id referencing the ticket table
    @JoinColumn(name = "ticket_id", nullable = false)
    // Holds the ticket entity reference this comment belongs to
    private Ticket ticket;

    // Instructs Hibernate to automatically generate current timestamp upon insertion
    @CreationTimestamp
    // Configures column as non-nullable and prevents updates after initial insert
    @Column(nullable = false, updatable = false)
    // Stores the exact timestamp when this comment was created
    private LocalDateTime createdAt;
// Closes the Comment entity class definition
}
