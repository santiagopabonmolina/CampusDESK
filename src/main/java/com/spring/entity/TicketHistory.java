// Defines the package namespace containing JPA persistence entities
package com.spring.entity;

// Imports the Ticket entity referenced by this audit history record
import com.spring.entity.Ticket;
// Imports the TicketState enum representing prior and updated states
import com.spring.entity.enums.TicketState;
// Imports the User entity representing the actor making status changes
import com.spring.entity.User;
// Imports Jakarta Persistence Column annotation for configuring database columns
import jakarta.persistence.Column;
// Imports Jakarta Persistence Entity annotation marking class as managed entity
import jakarta.persistence.Entity;
// Imports EnumType enumeration specifying string mapping strategy for enums
import jakarta.persistence.EnumType;
// Imports Enumerated annotation binding Java enum properties to database columns
import jakarta.persistence.Enumerated;
// Imports FetchType defining lazy or eager relationship loading strategies
import jakarta.persistence.FetchType;
// Imports GeneratedValue annotation configuring primary key generation strategies
import jakarta.persistence.GeneratedValue;
// Imports GenerationType enumeration defining database identity generation logic
import jakarta.persistence.GenerationType;
// Imports Id annotation declaring the primary key of this entity
import jakarta.persistence.Id;
// Imports JoinColumn annotation defining foreign key column mappings
import jakarta.persistence.JoinColumn;
// Imports ManyToOne annotation establishing relational mapping to parent entity
import jakarta.persistence.ManyToOne;
// Imports PrePersist lifecycle callback annotation executed before record insert
import jakarta.persistence.PrePersist;
// Imports Table annotation specifying relational table mapping in database
import jakarta.persistence.Table;
// Imports Hibernate Immutable annotation marking entity records as write-once audit logs
import org.hibernate.annotations.Immutable;

// Imports Java time class for storing audit event occurrence timestamps
import java.time.LocalDateTime;

/**
 * Immutable audit record of a Ticket status change.
 * Hibernate never issues UPDATE statements for this entity.
 */
// Marks this class as a persistent entity within Spring Data JPA
@Entity
// Disallows database update statements ensuring audit logs remain immutable
@Immutable
// Maps this audit entity to the database table named ticket_history
@Table(name = "ticket_history")
// Declares the TicketHistory audit entity tracking ticket status lifecycle changes
public class TicketHistory {

    // Designates the primary key property for this audit record
    @Id
    // Generates sequential numeric identifiers using PostgreSQL auto-incrementing identity
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Holds the unique identifier for this ticket history record
    private Long id;

    // Defines non-optional lazy association to parent ticket for audit trace
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    // Maps non-updatable foreign key column ticket_id to the ticket table
    @JoinColumn(name = "ticket_id", nullable = false, updatable = false)
    // Stores the ticket entity reference affected by this lifecycle event
    private Ticket ticket;

    // Maps previous ticket state enum value as string in database
    @Enumerated(EnumType.STRING)
    // Configures read-only column storing ticket status before the change occurred
    @Column(name = "previous_status", length = 30, updatable = false)
    // Holds the status value prior to this state transition occurring
    private TicketState previousStatus;

    @Column(name = "TimeStatus", nullable = false, length = 30, updatable = false)
    private TicketState PrStatus;

    // Maps new ticket state enum value as string in database
    @Enumerated(EnumType.STRING)
    // Configures required read-only column storing the updated ticket status value
    @Column(name = "new_status", nullable = false, length = 30, updatable = false)
    // Holds the updated status value resulting from this state transition
    private TicketState newStatus;

    // Defines non-optional lazy association to user who made this change
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    // Maps non-updatable foreign key column changed_by referencing users table
    @JoinColumn(name = "changed_by", nullable = false, updatable = false)
    // References the user entity responsible for triggering this status transition
    private User changedBy;

    // Configures immutable column storing timestamp when the transition took place
    @Column(name = "changed_at", nullable = false, updatable = false)
    // Stores the exact timestamp when this status modification occurred
    private LocalDateTime changedAt;

    // Protected default no-argument constructor required by JPA entity proxying
    protected TicketHistory() {
    // Closes default constructor
    }

    // Parameterized constructor initializing required immutable state transition audit attributes
    public TicketHistory(Ticket ticket, TicketState previousStatus, TicketState newStatus, User changedBy) {
        // Assigns the target ticket undergoing the state transition to field
        this.ticket = ticket;
        // Assigns the status prior to the transition to field
        this.previousStatus = previousStatus;
        // Assigns the new status resulting from the transition to field
        this.newStatus = newStatus;
        // Assigns the user who authorized or performed the change to field
        this.changedBy = changedBy;
    // Closes parameterized constructor
    }

    /**
     * Sets timestamp before persist.
     */
    // Registers lifecycle callback method to execute before database entity insertion
    @PrePersist
    // Executes automatic assignment of current timestamp if not previously populated
    void onCreate() {
        // Checks whether the timestamp field was left uninitialized before insertion
        if (this.changedAt == null) {
            // Sets the changed timestamp to the current local date and time
            this.changedAt = LocalDateTime.now();
        // Closes if condition block
        }
    // Closes onCreate method block
    }

    /**
     * Gets record unique identifier.
     */
    // Returns the primary key numeric identifier of this audit record
    public Long getId() {
        // Retrieves the database primary key value for this record
        return id;
    // Closes getId method
    }

    /**
     * Gets associated ticket entity.
     */
    // Returns the associated ticket entity linked to this history record
    public Ticket getTicket() {
        // Retrieves the target ticket entity associated with this history event
        return ticket;
    // Closes getTicket method
    }

    /**
     * Gets previous ticket status.
     */
    // Returns the state of the ticket prior to this transition
    public TicketState getPreviousStatus() {
        // Retrieves the prior ticket status recorded before this transition event
        return previousStatus;
    // Closes getPreviousStatus method
    }

    /**
     * Gets new ticket status.
     */
    // Returns the updated state of the ticket after this transition
    public TicketState getNewStatus() {
        // Retrieves the new ticket status resulting from this transition event
        return newStatus;
    // Closes getNewStatus method
    }

    /**
     * Gets change author user.
     */
    // Returns the user entity who performed this status change action
    public User getChangedBy() {
        // Retrieves the user instance that triggered this status change action
        return changedBy;
    // Closes getChangedBy method
    }

    /**
     * Gets change timestamp date.
     */
    // Returns the timestamp indicating when this status change occurred
    public LocalDateTime getChangedAt() {
        // Retrieves the exact date and time when this record occurred
        return changedAt;
    // Closes getChangedAt method
    }
// Closes TicketHistory class definition
}
