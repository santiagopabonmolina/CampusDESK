package com.spring.service;

import com.spring.entity.EmailVerification;
import com.spring.entity.User;
import com.spring.repository.EmailVerificationRepository;
import com.spring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationService {

    private static final int CODE_EXPIRATION_MINUTES = 10;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    private final UserRepository userRepository;
    private final EmailVerificationRepository verificationRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    private String generateSecureCode() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }

    @Transactional
    public void createAndSendVerificationCode(User user) {
        String code = generateSecureCode();
        String codeHash = passwordEncoder.encode(code);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusMinutes(CODE_EXPIRATION_MINUTES);

        EmailVerification verification = verificationRepository.findByUser(user)
                .orElse(EmailVerification.builder()
                        .user(user)
                        .build());

        verification.setCodeHash(codeHash);
        verification.setExpiryDate(expiry);
        verification.setFailedAttempts(0);
        verification.setInvalidated(false);
        verification.setLastResendAt(now);

        verificationRepository.save(verification);
        emailService.sendVerificationEmail(user.getEmail(), code);
    }

    @Transactional
    public void verifyEmail(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (Boolean.TRUE.equals(user.getVerified())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cuenta ya ha sido verificada");
        }

        EmailVerification verification = verificationRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe un código de verificación activo para este correo. Solicita uno nuevo."));

        if (Boolean.TRUE.equals(verification.getInvalidated())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código ha sido invalidado por exceso de intentos o expiración. Solicita uno nuevo.");
        }

        if (verification.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
            verification.setInvalidated(true);
            verificationRepository.save(verification);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Has superado el límite de 5 intentos fallidos. Solicita un nuevo código.");
        }

        if (LocalDateTime.now().isAfter(verification.getExpiryDate())) {
            verification.setInvalidated(true);
            verificationRepository.save(verification);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código ha expirado (validez de 10 minutos). Solicita un nuevo código.");
        }

        if (!passwordEncoder.matches(code, verification.getCodeHash())) {
            int attempts = verification.getFailedAttempts() + 1;
            verification.setFailedAttempts(attempts);

            if (attempts >= MAX_FAILED_ATTEMPTS) {
                verification.setInvalidated(true);
                verificationRepository.save(verification);
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Código incorrecto. Has superado el límite máximo de intentos (5). El código fue invalidado.");
            }

            verificationRepository.save(verification);
            int remaining = MAX_FAILED_ATTEMPTS - attempts;
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código de verificación incorrecto. Te quedan " + remaining + " intento(s).");
        }

        // Verification successful
        user.setVerified(true);
        userRepository.save(user);

        verificationRepository.delete(verification);
        log.info("Usuario {} verificado exitosamente", email);
    }

    @Transactional
    public void resendCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (Boolean.TRUE.equals(user.getVerified())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cuenta ya ha sido verificada");
        }

        EmailVerification verification = verificationRepository.findByUser(user).orElse(null);
        if (verification != null && verification.getLastResendAt() != null) {
            long secondsPassed = Duration.between(verification.getLastResendAt(), LocalDateTime.now()).getSeconds();
            if (secondsPassed < RESEND_COOLDOWN_SECONDS) {
                long waitTime = RESEND_COOLDOWN_SECONDS - secondsPassed;
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Debes esperar " + waitTime + " segundos antes de reenviar el código.");
            }
        }

        createAndSendVerificationCode(user);
    }
}
