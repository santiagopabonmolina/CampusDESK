package com.spring.modules.ticketHistory;

import com.spring.modules.ticket.ticket;
import com.spring.modules.ticket.ticketStatus; // ASSUMPTION: enum in the ticket module
import com.spring.modules.user.User;           // ASSUMPTION: user entity is named User
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/**
 * Immutable audit record of a ticket status change.
 * Hibernate never issues UPDATE statements for this entity.
 */
@Entity
@Immutable
@Table(name = "ticket_history")
public class ticketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false, updatable = false)
    private ticket ticket;

    // Null only for the initial record created together with the ticket.
    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 30, updatable = false)
    private ticketStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30, updatable = false)
    private ticketStatus newStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by", nullable = false, updatable = false)
    private User changedBy;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    protected ticketHistory() {
        // Required by JPA
    }

    public ticketHistory(ticket ticket, ticketStatus previousStatus, ticketStatus newStatus, User changedBy) {
        this.ticket = ticket;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
    }

    @PrePersist
    void onCreate() {
        if (this.changedAt == null) {
            this.changedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public ticket getTicket() {
        return ticket;
    }

    public ticketStatus getPreviousStatus() {
        return previousStatus;
    }

    public ticketStatus getNewStatus() {
        return newStatus;
    }

    public User getChangedBy() {
        return changedBy;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}
