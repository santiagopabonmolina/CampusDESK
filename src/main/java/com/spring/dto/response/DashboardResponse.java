// Defines the package namespace containing outbound server response DTOs
package com.spring.dto.response;

// Imports Lombok annotation generating constructor taking all class field arguments
import lombok.AllArgsConstructor;
// Imports Lombok annotation implementing the fluent builder creation pattern
import lombok.Builder;
// Imports Lombok annotation generating getters, setters, toString, equals, and hashcode
import lombok.Data;
// Imports Lombok annotation generating zero-argument default public constructor
import lombok.NoArgsConstructor;

// Generates boilerplate data access methods across all dashboard metric attributes
@Data
// Implements the builder pattern for constructing dashboard metric responses fluently
@Builder
// Generates empty default constructor required by serialization and deserialization
@NoArgsConstructor
// Generates constructor accepting values for all declared statistical counter fields
@AllArgsConstructor
// Declares data transfer object carrying aggregated ticket metrics to dashboard
public class DashboardResponse {
    // Holds the total count of tickets registered in the system
    private long totalTickets;
    // Holds the count of tickets currently in opened initial state
    private long opened;
    // Holds the count of tickets currently assigned to technicians
    private long assigned;
    // Holds the count of tickets actively in process by technicians
    private long inProcess;
    // Holds the count of tickets marked as resolved by technicians
    private long resolved;
    // Holds the count of tickets marked as closed by users
    private long closed;
// Closes the DashboardResponse class definition
}
