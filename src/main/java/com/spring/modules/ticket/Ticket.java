package com.spring.modules.ticket;

import com.spring.modules.comment.Comment;
import com.spring.modules.ticketHistory.TicketHistory;
import jakarta.persistence.*;
import com.spring.modules.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 225)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketState state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_request", nullable = false)
    private User request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    private User technicianAssigning;

    @Column(name = "create_date", updatable = false)
    private LocalDateTime createDate;

    @Column(name = "resolution_date")
    private LocalDateTime resolutionDate;

    @Column(name = "close_date")
    private LocalDateTime closeDate;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    private List<TicketHistory> history = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createDate = LocalDateTime.now();
        this.state = TicketState.OPENED;
    }

}
