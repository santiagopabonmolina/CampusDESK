package com.spring.service;

import com.spring.dto.response.DashboardResponse;
import com.spring.entity.User;
import com.spring.entity.enums.RoleName;
import com.spring.entity.enums.TicketState;
import com.spring.repository.TicketRepository;
import com.spring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardSummary() {
        return getDashboardSummary(null);
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardSummary(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return getGlobalCounts();
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isTech = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TECHNICIAN"));

        if (isAdmin) {
            return getGlobalCounts();
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return getGlobalCounts();
        }

        if (isTech) {
            Long techId = user.getId();
            return DashboardResponse.builder()
                    .totalTickets(ticketRepository.countByTechnicianAssignedId(techId))
                    .opened(ticketRepository.countByTechnicianAssignedIdAndState(techId, TicketState.OPENED))
                    .assigned(ticketRepository.countByTechnicianAssignedIdAndState(techId, TicketState.ASSIGNED))
                    .inProcess(ticketRepository.countByTechnicianAssignedIdAndState(techId, TicketState.IN_PROCESS))
                    .resolved(ticketRepository.countByTechnicianAssignedIdAndState(techId, TicketState.RESOLVED))
                    .closed(ticketRepository.countByTechnicianAssignedIdAndState(techId, TicketState.CLOSED))
                    .build();
        } else {
            // Rol USER: filtrar conteos por sus propios tickets solicitados
            Long requesterId = user.getId();
            return DashboardResponse.builder()
                    .totalTickets(ticketRepository.countByRequesterId(requesterId))
                    .opened(ticketRepository.countByRequesterIdAndState(requesterId, TicketState.OPENED))
                    .assigned(ticketRepository.countByRequesterIdAndState(requesterId, TicketState.ASSIGNED))
                    .inProcess(ticketRepository.countByRequesterIdAndState(requesterId, TicketState.IN_PROCESS))
                    .resolved(ticketRepository.countByRequesterIdAndState(requesterId, TicketState.RESOLVED))
                    .closed(ticketRepository.countByRequesterIdAndState(requesterId, TicketState.CLOSED))
                    .build();
        }
    }

    private DashboardResponse getGlobalCounts() {
        return DashboardResponse.builder()
                .totalTickets(ticketRepository.count())
                .opened(ticketRepository.countByState(TicketState.OPENED))
                .assigned(ticketRepository.countByState(TicketState.ASSIGNED))
                .inProcess(ticketRepository.countByState(TicketState.IN_PROCESS))
                .resolved(ticketRepository.countByState(TicketState.RESOLVED))
                .closed(ticketRepository.countByState(TicketState.CLOSED))
                .build();
    }
}
