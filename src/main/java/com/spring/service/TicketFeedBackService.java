package com.spring.service;

import com.spring.dto.request.TicketFeedBackRepository;
import com.spring.dto.response.TicketFeedBackResponse;
import com.spring.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
// Generates constructor injecting required final repository dependencies automatically
@RequiredArgsConstructor
public class TicketFeedBackService {

    private final TicketFeedBackRepository ticketFeedBackRepository;

    @Transactional(readOnly = true)
    // Overloaded method calculating global dashboard summary when no principal exists
    public TicketFeedBackResponse getAverage(Authentication authentication){
        if (authentication == null || !authentication.isAuthenticated()) {
            // Returns system-wide global counts for unauthenticated dashboard views
            return ...;
            // Closes if condition checking unauthenticated requests
        }

    }
}
