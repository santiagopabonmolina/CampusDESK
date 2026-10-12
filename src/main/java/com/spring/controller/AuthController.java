package com.spring.controller;

import com.spring.dto.request.LoginRequest;
import com.spring.dto.request.RegisterRequest;
import com.spring.dto.response.JwtResponse;
import com.spring.entity.Role;
import com.spring.entity.User;
import com.spring.entity.enums.RoleName;
import com.spring.repository.RoleRepository;
import com.spring.repository.UserRepository;
import com.spring.security.jwt.JwtTokenProvider;
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

import java.util.Map;
import java.util.Set;

@Tag(name = "1. Autenticación y Cuentas", description = "Registro de nuevos colaboradores e inicio de sesión con emisión de tokens JWT")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "Registrar nuevo usuario", description = "Crea una cuenta en la plataforma. Por política de seguridad empresarial, todo registro público recibe automáticamente el rol USER.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
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

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(userRole))
                .active(true)
                .build();

        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Usuario registrado exitosamente"));
    }

    @Operation(summary = "Iniciar sesión", description = "Autentica credenciales en PostgreSQL contra hash BCrypt y genera el token de acceso JWT con los roles del usuario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa, retorna token JWT y datos de sesión"),
            @ApiResponse(responseCode = "400", description = "Formato de petición inválido"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas (correo o contraseña inválidos)")
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
