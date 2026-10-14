package com.spring.controller;

import com.spring.dto.request.LoginRequest;
import com.spring.dto.request.RegisterRequest;
import com.spring.dto.request.ResendCodeRequest;
import com.spring.dto.request.VerifyCodeRequest;
import com.spring.dto.response.JwtResponse;
import com.spring.entity.Role;
import com.spring.entity.User;
import com.spring.entity.enums.RoleName;
import com.spring.repository.RoleRepository;
import com.spring.repository.UserRepository;
import com.spring.security.jwt.JwtTokenProvider;
import com.spring.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Tag(name = "1. Autenticación y Cuentas", description = "Registro de nuevos colaboradores con verificación por correo de 6 dígitos e inicio de sesión con JWT")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Operation(summary = "Registrar nuevo usuario con código de verificación", description = "Crea una cuenta en la plataforma en estado pendiente y envía un código de 6 dígitos al correo electrónico.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente. Código enviado al correo."),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o incompletos"),
            @ApiResponse(responseCode = "409", description = "El correo electrónico ya se encuentra registrado")
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "El correo ya está en uso"));
        }

        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.USER).build()));

        // Generar código de 6 dígitos numérico
        String verificationCode = String.format("%06d", RANDOM.nextInt(1_000_000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(userRole))
                .active(false) // Inactivo hasta que verifique el código enviado a su correo
                .verificationCode(verificationCode)
                .verificationCodeExpiresAt(expiresAt)
                .build();

        userRepository.save(user);

        // Envío asíncrono o síncrono del código por correo
        emailService.sendVerificationCode(user.getEmail(), user.getFirstName(), verificationCode);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "Usuario registrado exitosamente. Te hemos enviado un código de 6 dígitos a tu correo electrónico.",
                        "email", user.getEmail(),
                        "devCode", verificationCode // Visible para facilitar pruebas locales sin SMTP
                ));
    }

    @Operation(summary = "Verificar código de registro", description = "Valida el código de 6 dígitos recibido por correo y activa la cuenta del usuario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta verificada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Código incorrecto o expirado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No se encontró ningún usuario con el correo proporcionado"));
        }

        if (Boolean.TRUE.equals(user.getActive())) {
            return ResponseEntity.ok(Map.of(
                    "message", "Tu cuenta ya se encuentra verificada. Ya puedes iniciar sesión.",
                    "alreadyVerified", true
            ));
        }

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(request.getCode().trim())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "El código de verificación ingresado es incorrecto"));
        }

        if (user.getVerificationCodeExpiresAt() != null && user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "El código de verificación ha expirado. Por favor solicita uno nuevo."));
        }

        // Activar usuario y limpiar código
        user.setActive(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "¡Cuenta verificada exitosamente! Ya puedes iniciar sesión en CampusDESK.",
                "verified", true
        ));
    }

    @Operation(summary = "Reenviar código de verificación", description = "Genera un nuevo código de 6 dígitos y lo reenvía al correo registrado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nuevo código enviado"),
            @ApiResponse(responseCode = "400", description = "La cuenta ya está activa"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PostMapping("/resend-code")
    public ResponseEntity<?> resendCode(@Valid @RequestBody ResendCodeRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No se encontró ningún usuario con el correo proporcionado"));
        }

        if (Boolean.TRUE.equals(user.getActive())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "La cuenta ya se encuentra verificada. Puedes iniciar sesión directamente."));
        }

        String newCode = String.format("%06d", RANDOM.nextInt(1_000_000));
        user.setVerificationCode(newCode);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        emailService.sendVerificationCode(user.getEmail(), user.getFirstName(), newCode);

        return ResponseEntity.ok(Map.of(
                "message", "Se ha enviado un nuevo código de verificación a tu correo.",
                "email", user.getEmail(),
                "devCode", newCode
        ));
    }

    @Operation(summary = "Iniciar sesión", description = "Autentica credenciales en PostgreSQL contra hash BCrypt y genera el token de acceso JWT con los roles del usuario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa, retorna token JWT y datos de sesión"),
            @ApiResponse(responseCode = "400", description = "Formato de petición inválido"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas o cuenta inactiva sin verificar")
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean hasAdmin = user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ADMIN);
        boolean hasTech = user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.TECHNICIAN);
        String roleStr = hasAdmin ? "ADMIN" : (hasTech ? "TECHNICIAN" : "USER");

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
}
