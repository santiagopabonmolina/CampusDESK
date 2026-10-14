package com.spring.repository;

import com.spring.entity.EmailVerification;
import com.spring.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByUser(User user);
    Optional<EmailVerification> findByUserId(Long userId);
    void deleteByUser(User user);
}
