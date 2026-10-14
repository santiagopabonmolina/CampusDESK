package com.spring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "code_hash", nullable = false, length = 255)
    private String codeHash;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Builder.Default
    @Column(name = "failed_attempts", nullable = false)
    private Integer failedAttempts = 0;

    @Column(name = "last_resend_at")
    private LocalDateTime lastResendAt;

    @Builder.Default
    @Column(nullable = false)
    private Boolean invalidated = false;

    @PrePersist
    protected void onCreate() {
        if (this.failedAttempts == null) {
            this.failedAttempts = 0;
        }
        if (this.invalidated == null) {
            this.invalidated = false;
        }
    }
}
