package com.spring.service;

import com.spring.dto.response.DashboardResponse;
import com.spring.entity.enums.TicketState;
import com.spring.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardSummary() {
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
