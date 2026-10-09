package com.spring.repository;

import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Extends the marker Repository (not JpaRepository) on purpose, so that
 * delete and update operations are not exposed for historical records.
 */
public interface TicketHistoryRepository extends Repository<TicketHistory, Long> {

    TicketHistory save(TicketHistory entry);

    List<TicketHistory> findByTicketIdOrderByChangedAtAscIdAsc(Long TicketId);

    Optional<TicketHistory> findFirstByTicketIdOrderByChangedAtDescIdDesc(Long TicketId);
}
