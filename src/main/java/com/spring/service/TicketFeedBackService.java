package com.spring.service;

import com.spring.dto.request.TicketFeedBackRequest;
import com.spring.dto.response.TicketFeedBackResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
// Generates constructor injecting required final repository dependencies automatically
@RequiredArgsConstructor
public class TicketFeedBackService {

    private final TicketFeedBackRequest ticketFeedBackRequest;

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
