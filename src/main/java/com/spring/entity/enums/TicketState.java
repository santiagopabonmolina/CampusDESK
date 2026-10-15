// Defines the package namespace grouping enumeration types for entity attributes
package com.spring.entity.enums;

// Declares the enum representing the sequential workflow states of a ticket
public enum TicketState {
    // Indicates that a new ticket was successfully registered and awaits assignment
    OPENED,
    // Indicates that the ticket has been assigned to a specific technician
    ASSIGNED,
    // Indicates that the designated technician is actively diagnosing or resolving the ticket
    IN_PROCESS,
    // Indicates that the technical solution was applied and awaits requester confirmation
    RESOLVED,
    // Indicates that the issue lifecycle has been completed and formally closed
    CLOSED
// Closes the TicketState enum definition
}
