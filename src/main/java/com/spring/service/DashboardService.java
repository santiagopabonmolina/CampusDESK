// Defines the package namespace containing business service layer classes
package com.spring.service;

// Imports DashboardResponse DTO carrying aggregated dashboard ticket statistics
import com.spring.dto.response.DashboardResponse;
// Imports User JPA entity representing user account records
import com.spring.entity.User;
// Imports RoleName enum defining system security authorization roles
import com.spring.entity.enums.RoleName;
// Imports TicketState enum defining ticket workflow lifecycle states
import com.spring.entity.enums.TicketState;
// Imports TicketRepository interface providing ticket query and count operations
import com.spring.repository.TicketRepository;
// Imports UserRepository interface providing user lookup and query operations
import com.spring.repository.UserRepository;
// Imports Lombok annotation generating constructor for final dependency fields
import lombok.RequiredArgsConstructor;
// Imports Spring Security Authentication interface representing authenticated principal
import org.springframework.security.core.Authentication;
// Imports Spring Service stereotype annotation declaring business service component
import org.springframework.stereotype.Service;
// Imports Spring Transactional annotation managing read-only transaction context
import org.springframework.transaction.annotation.Transactional;

// Marks this class as Spring service component for dashboard metrics
@Service
// Automatically generates constructor injecting required final repository dependencies
@RequiredArgsConstructor
// Declares DashboardService computing role-tailored dashboard metrics and summaries
public class DashboardService {

    // Declares final repository reference for executing ticket aggregation queries
    private final TicketRepository ticketRepository;
    // Declares final repository reference for looking up authenticated user records
    private final UserRepository userRepository;

    /**
     * Retrieves default dashboard metrics.
     */
    // Configures read-only transactional boundary optimizing performance for dashboard queries
    @Transactional(readOnly = true)
    // Overloaded method calculating global dashboard summary when no principal exists
    public DashboardResponse getDashboardSummary() {
        // Delegates to main getDashboardSummary method passing null authentication
        return getDashboardSummary(null);
    // Closes getDashboardSummary overloaded method
    }

    /**
     * Calculates role dashboard summary.
     */
    // Configures read-only transaction avoiding unnecessary database snapshot overhead
    @Transactional(readOnly = true)
    // Computes customized ticket statistics tailored to authenticated user's role
    public DashboardResponse getDashboardSummary(Authentication authentication) {
        // Evaluates whether request comes from unauthenticated or null security context
        if (authentication == null || !authentication.isAuthenticated()) {
            // Returns system-wide global counts for unauthenticated dashboard views
            return getGlobalCounts();
        // Closes if condition checking unauthenticated requests
        }

        // Checks whether current user possesses administrator authority role
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        // Checks whether current user possesses technician authority role
        boolean isTech = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TECHNICIAN"));

        // Checks whether authenticated user has administrator privileges
        if (isAdmin) {
            // Returns total global counts across all campus users for administrators
            return getGlobalCounts();
        // Closes if condition for administrator role
        }

        // Extracts username email of authenticated user from security principal
        String email = authentication.getName();
        // Finds user entity by email or returns null if not found
        User user = userRepository.findByEmail(email).orElse(null);
        // Validates whether user account exists in database repository
        if (user == null) {
            // Returns global counts fallback if user record is missing
            return getGlobalCounts();
        // Closes if condition checking user null
        }

        // Checks whether authenticated user has technician authorization role
        if (isTech) {
            // Retrieves technician's unique database primary key ID
            Long techId = user.getId();
            // Builds DashboardResponse aggregating counts of tickets assigned to technician
            return DashboardResponse.builder()
                    .totalTickets(ticketRepository.countByTechnicianAssignedId(techId))
                    .opened(ticketRepository.countByTechnicianAssignedIdAndState(techId, TicketState.OPENED))
                    .assigned(ticketRepository.countByTechnicianAssignedIdAndState(techId, TicketState.ASSIGNED))
                    .inProcess(ticketRepository.countByTechnicianAssignedIdAndState(techId, TicketState.IN_PROCESS))
                    .resolved(ticketRepository.countByTechnicianAssignedIdAndState(techId, TicketState.RESOLVED))
                    .closed(ticketRepository.countByTechnicianAssignedIdAndState(techId, TicketState.CLOSED))
                    .build();
        // Handles standard user role aggregating tickets created by requester
        } else {
            // Retrieves requester's unique database primary key ID
            Long requesterId = user.getId();
            // Builds DashboardResponse aggregating counts of tickets created by requester
            return DashboardResponse.builder()
                    .totalTickets(ticketRepository.countByRequesterId(requesterId))
                    .opened(ticketRepository.countByRequesterIdAndState(requesterId, TicketState.OPENED))
                    .assigned(ticketRepository.countByRequesterIdAndState(requesterId, TicketState.ASSIGNED))
                    .inProcess(ticketRepository.countByRequesterIdAndState(requesterId, TicketState.IN_PROCESS))
                    .resolved(ticketRepository.countByRequesterIdAndState(requesterId, TicketState.RESOLVED))
                    .closed(ticketRepository.countByRequesterIdAndState(requesterId, TicketState.CLOSED))
                    .build();
        // Closes else block for requester metrics
        }
    // Closes getDashboardSummary method
    }

    /**
     * Calculates global ticket counts.
     */
    // Private helper method computing system-wide ticket counts across all states
    private DashboardResponse getGlobalCounts() {
        // Builds DashboardResponse aggregating system-wide ticket counts by state
        return DashboardResponse.builder()
                .totalTickets(ticketRepository.count())
                .opened(ticketRepository.countByState(TicketState.OPENED))
                .assigned(ticketRepository.countByState(TicketState.ASSIGNED))
                .inProcess(ticketRepository.countByState(TicketState.IN_PROCESS))
                .resolved(ticketRepository.countByState(TicketState.RESOLVED))
                .closed(ticketRepository.countByState(TicketState.CLOSED))
                .build();
    // Closes getGlobalCounts helper method
    }
// Closes DashboardService class definition
}
