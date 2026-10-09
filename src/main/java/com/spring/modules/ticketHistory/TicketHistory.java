package com.spring.modules.TicketHistory;

import com.spring.modules.Ticket.Ticket;
import com.spring.modules.Ticket.TicketStatus; // ASSUMPTION: enum in the Ticket module
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
 * Immutable audit record of a Ticket status change.
 * Hibernate never issues UPDATE statements for this entity.
 */
@Entity
@Immutable
@Table(name = "Ticket_history")
public class TicketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Ticket_id", nullable = false, updatable = false)
    private Ticket Ticket;

    // Null only for the initial record created together with the Ticket.
    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 30, updatable = false)
    private TicketStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30, updatable = false)
    private TicketStatus newStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by", nullable = false, updatable = false)
    private User changedBy;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    protected TicketHistory() {
        // Required by JPA
    }

    public TicketHistory(Ticket Ticket, TicketStatus previousStatus, TicketStatus newStatus, User changedBy) {
        this.Ticket = Ticket;
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

    public Ticket getTicket() {
        return Ticket;
    }

    public TicketStatus getPreviousStatus() {
        return previousStatus;
    }

    public TicketStatus getNewStatus() {
        return newStatus;
    }

    public User getChangedBy() {
        return changedBy;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}
