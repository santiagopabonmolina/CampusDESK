// Defines the package namespace containing JPA persistence entities
package com.spring.entity;

// Imports TicketCategory enum for classifying incident domain types
import com.spring.entity.enums.TicketCategory;
// Imports TicketPriority enum for ranking urgency levels of tickets
import com.spring.entity.enums.TicketPriority;
// Imports TicketState enum for tracking the progression stages of tickets
import com.spring.entity.enums.TicketState;
// Imports Jakarta Persistence API annotations for object-relational mapping
import jakarta.persistence.*;
// Imports Lombok annotations to automatically generate getters, setters, and builders
import lombok.*;

// Imports Java time class representing calendar date and local time
import java.time.LocalDateTime;
// Imports ArrayList class to initialize collection fields with empty lists
import java.util.ArrayList;
// Imports List interface representing an ordered collection of objects
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Marks this class as a persistent entity within Spring Data JPA
@Entity
// Maps this entity to the relational database table named ticket
@Table(name = "ticket")
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
// Declares the Ticket entity representing support requests in CampusDESK
public class Ticket {

    // Marks this field as the primary key of the ticket entity
    @Id
    // Configures auto-incrementing identity column strategy supported by PostgreSQL
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Stores the unique database numeric identifier for this ticket
    private Long id;

    // Configures required non-nullable varchar column with maximum length 225
    @Column(nullable = false, length = 225)
    // Stores the concise descriptive title of the submitted technical issue
    private String title;

    // Configures required non-nullable column mapped to PostgreSQL TEXT datatype
    @Column(nullable = false, columnDefinition = "TEXT")
    // Stores the detailed comprehensive description of the incident reported
    private String description;

    // Persists the enum value as a string representation in database
    @Enumerated(EnumType.STRING)
    // Configures required non-nullable varchar column with length 30 for category
    @Column(nullable = false, length = 30)
    // Stores the classified incident category assigned to this ticket
    private TicketCategory category;

    // Persists the enum value as a string representation in database
    @Enumerated(EnumType.STRING)
    // Configures required non-nullable varchar column with length 20 for priority
    @Column(nullable = false, length = 20)
    // Stores the urgency and severity priority level of this ticket
    private TicketPriority priority;

    // Persists the enum value as a string representation in database
    @Enumerated(EnumType.STRING)
    // Configures required non-nullable varchar column with length 30 for state
    @Column(nullable = false, length = 30)
    // Stores the current workflow lifecycle status of this ticket
    private TicketState state;

    // Defines many-to-one relationship using lazy loading for ticket requester
    @ManyToOne(fetch = FetchType.LAZY)
    // Maps mandatory foreign key column requester_id referencing users table
    @JoinColumn(name = "requester_id", nullable = false)
    // References the user entity who created this support request
    private User requester;

    // Defines many-to-one relationship using lazy loading for assigned technician
    @ManyToOne(fetch = FetchType.LAZY)
    // Maps optional foreign key column technician_id referencing users table
    @JoinColumn(name = "technician_id")
    // References the technician user entity assigned to resolve this ticket
    private User technicianAssigned;

    // Maps creation timestamp column and prevents modification after insertion
    @Column(name = "create_date", updatable = false)
    // Stores the date and time when this ticket was first opened
    private LocalDateTime createDate;

    // Maps resolution timestamp column populated when ticket reaches resolved status
    @Column(name = "resolution_date")
    // Stores the date and time when the technician marked ticket resolved
    private LocalDateTime resolutionDate;

    // Maps closing timestamp column populated when ticket reaches closed status
    @Column(name = "close_date")
    // Stores the date and time when the ticket was formally closed
    private LocalDateTime closeDate;

    // Instructs Lombok builder to retain initialized ArrayList for comments collection
    @Builder.Default
    // Maps bidirectional one-to-many relationship with cascading lifecycle and orphan removal
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    // Holds the collection of comments associated with this ticket
    private List<Comment> comments = new ArrayList<>();

    // Instructs Lombok builder to retain initialized ArrayList for history collection
    @Builder.Default
    // Maps bidirectional one-to-many relationship with cascading lifecycle and orphan removal
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    // Holds the collection of audit history transitions recorded for this ticket
    private List<TicketHistory> history = new ArrayList<>();

    @Builder.Default
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "ticket_tag",
            joinColumns = @JoinColumn(name = "ticket_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
//

    /**
     * Sets defaults before persist.
     */
    // Registers lifecycle callback invoked before entity record is persisted
    @PrePersist
    // Initializes default creation timestamp and opened state before persisting
    protected void onCreate() {
        // Sets creation date to current local date and time
        this.createDate = LocalDateTime.now();
        // Evaluates whether status was unassigned prior to entity persist
        if (this.state == null) {
            // Defaults initial ticket state to OPENED upon creation
            this.state = TicketState.OPENED;
        // Closes if condition block
        }
    // Closes onCreate callback method
    }

// Closes Ticket class definition
}
