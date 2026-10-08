package com.spring.modules.ticketHistory;

import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Extends the marker Repository (not JpaRepository) on purpose, so that
 * delete and update operations are not exposed for historical records.
 */
public interface ticketHistoryRepository extends Repository<ticketHistory, Long> {

    ticketHistory save(ticketHistory entry);

    List<ticketHistory> findByTicketIdOrderByChangedAtAscIdAsc(Long ticketId);

    Optional<ticketHistory> findFirstByTicketIdOrderByChangedAtDescIdDesc(Long ticketId);
}
