package com.spring.controller;

import com.spring.dto.request.LoginRequest;
import com.spring.dto.request.RegisterRequest;
import com.spring.dto.request.ResendCodeRequest;
import com.spring.dto.request.VerifyEmailRequest;
import com.spring.dto.response.JwtResponse;
import com.spring.entity.Role;
import com.spring.entity.User;
import com.spring.entity.enums.RoleName;
import com.spring.repository.RoleRepository;
import com.spring.repository.UserRepository;
import com.spring.security.jwt.JwtTokenProvider;
import com.spring.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final VerificationService verificationService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "El correo ya está en uso"));
        }

        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.USER).build()));

        User user = User.builder()
                .firstName(request.getFristName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(userRole))
                .active(true)
                .verified(false) // Siempre no verificado al registrarse
                .build();

        User savedUser = userRepository.save(user);

        // Generar y enviar código de verificación por correo
        verificationService.createAndSendVerificationCode(savedUser);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "Usuario registrado exitosamente. Por favor verifica tu correo.",
                        "email", savedUser.getEmail(),
                        "requiresVerification", true
                ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // Rechazar inicio de sesión si la cuenta no ha sido verificada
        if (!Boolean.TRUE.equals(user.getVerified())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message", "Debes verificar tu correo",
                            "email", user.getEmail(),
                            "verified", false
                    ));
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String roleStr = user.getRoles().stream().findFirst().map(r -> r.getName().name()).orElse("USER");
        String jwt = jwtTokenProvider.generateToken(authentication, user.getId(), user.getFirstName(), user.getLastName());

        return ResponseEntity.ok(JwtResponse.builder()
                .token(jwt)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role("ROLE_" + roleStr)
                .build());
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        verificationService.verifyEmail(request.getEmail(), request.getCode());
        return ResponseEntity.ok(Map.of("message", "Correo verificado exitosamente. Ya puedes iniciar sesión."));
    }

    @PostMapping("/resend-code")
    public ResponseEntity<?> resendCode(@Valid @RequestBody ResendCodeRequest request) {
        verificationService.resendCode(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Código reenviado exitosamente a tu correo."));
    }
}
