package com.spring.repository;

import com.spring.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketFeedBackRespository extends JpaRepository<Ticket, Long> {

}
