package com.spring.repository;

import com.spring.entity.TicketHistory;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Extends the marker Repository (not JpaRepository) on purpose, so that
 * delete and update operations are not exposed for historical records.
 */
public interface TicketHistoryRepository extends Repository<TicketHistory, Long> {

    /**
     * Saves ticket history record.
     */
    TicketHistory save(TicketHistory entry);

    /**
     * Finds ticket history records.
     */
    List<TicketHistory> findByTicketIdOrderByChangedAtAscIdAsc(Long ticketId);

    /**
     * Finds latest ticket history.
     */
    Optional<TicketHistory> findFirstByTicketIdOrderByChangedAtDescIdDesc(Long ticketId);
}
