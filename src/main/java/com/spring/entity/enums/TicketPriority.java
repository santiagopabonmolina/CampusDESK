// Defines the package namespace grouping enumeration types for entity attributes
package com.spring.entity.enums;

// Declares the enum defining urgency levels for prioritizing technical support tickets
public enum TicketPriority {
    // Designates non-urgent issues that do not disrupt normal daily academic activities
    LOW,
    // Designates normal priority issues affecting individual workflow with available workarounds
    MEDIUM,
    // Designates high urgency issues impairing critical workflows without straightforward alternatives
    HIGH,
    // Designates critical emergencies causing campus-wide outages requiring immediate technician intervention
    CRITIC
// Closes the TicketPriority enum definition
}
