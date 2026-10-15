// Defines the package namespace containing Spring MVC REST controllers
package com.spring.controller;

// Imports LoginRequest DTO carrying user credentials for authentication
import com.spring.dto.request.LoginRequest;
// Imports RegisterRequest DTO carrying user registration form data
import com.spring.dto.request.RegisterRequest;
// Imports JwtResponse DTO returning JSON Web Token and user details
import com.spring.dto.response.JwtResponse;
// Imports Role entity representing user security role records
import com.spring.entity.Role;
// Imports User entity representing user account records
import com.spring.entity.User;
// Imports RoleName enum representing supported system authorization roles
import com.spring.entity.enums.RoleName;
// Imports RoleRepository interface providing role database operations
import com.spring.repository.RoleRepository;
// Imports UserRepository interface providing user database operations
import com.spring.repository.UserRepository;
// Imports JwtTokenProvider utility component for generating and validating tokens
import com.spring.security.jwt.JwtTokenProvider;
// Imports Swagger Operation annotation describing REST endpoint functionality
import io.swagger.v3.oas.annotations.Operation;
// Imports Swagger ApiResponse annotation detailing possible HTTP responses
import io.swagger.v3.oas.annotations.responses.ApiResponse;
// Imports Swagger ApiResponses annotation grouping multiple API response annotations
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// Imports Swagger Tag annotation categorizing controllers in OpenAPI documentation
import io.swagger.v3.oas.annotations.tags.Tag;
// Imports Jakarta Valid annotation triggering bean validation on request payloads
import jakarta.validation.Valid;
// Imports Lombok annotation generating constructor for final dependency fields
import lombok.RequiredArgsConstructor;
// Imports HttpStatus enum representing standard HTTP status response codes
import org.springframework.http.HttpStatus;
// Imports ResponseEntity representing full HTTP responses including headers and body
import org.springframework.http.ResponseEntity;
// Imports AuthenticationManager interface processing authentication requests in Spring
import org.springframework.security.authentication.AuthenticationManager;
// Imports UsernamePasswordAuthenticationToken representing credentials token for authentication
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// Imports Spring Security Authentication interface representing authenticated principal
import org.springframework.security.core.Authentication;
// Imports SecurityContextHolder providing access to the current security context
import org.springframework.security.core.context.SecurityContextHolder;
// Imports PasswordEncoder interface for hashing and verifying passwords securely
import org.springframework.security.crypto.password.PasswordEncoder;
// Imports PostMapping annotation mapping HTTP POST requests onto handler methods
import org.springframework.web.bind.annotation.PostMapping;
// Imports RequestBody annotation binding HTTP request body to method parameter
import org.springframework.web.bind.annotation.RequestBody;
// Imports RequestMapping annotation mapping web requests onto controller classes
import org.springframework.web.bind.annotation.RequestMapping;
// Imports RestController annotation declaring this class as RESTful controller bean
import org.springframework.web.bind.annotation.RestController;

// Imports Map interface for returning quick structured JSON response messages
import java.util.Map;
// Imports Set interface representing an unordered collection of unique elements
import java.util.Set;

// Categorizes authentication endpoints under section one in OpenAPI UI
@Tag(name = "1. Autenticación y Cuentas", description = "Registro de nuevos colaboradores e inicio de sesión con emisión de tokens JWT")
// Marks this class as REST controller serializing responses to JSON format
@RestController
// Maps base URL route for authentication endpoints to api auth path
@RequestMapping("/api/auth")
// Generates constructor injecting required final dependencies automatically via Lombok
@RequiredArgsConstructor
// Declares AuthController handling user registration, authentication, and token generation
public class AuthController {

    // Declares final authentication manager for authenticating submitted user credentials
    private final AuthenticationManager authenticationManager;
    // Declares final user repository for querying and saving user records
    private final UserRepository userRepository;
    // Declares final role repository for looking up default user roles
    private final RoleRepository roleRepository;
    // Declares final password encoder for hashing passwords with BCrypt algorithm
    private final PasswordEncoder passwordEncoder;
    // Declares final JWT token provider for minting signed authentication tokens
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Registers a new user.
     */
    // Documents registration endpoint purpose and default role behavior in Swagger
    @Operation(summary = "Registrar nuevo usuario", description = "Crea una cuenta en la plataforma. Por política de seguridad empresarial, todo registro público recibe automáticamente el rol USER.")
    // Configures OpenAPI responses for successful creation, bad input, and conflict
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o incompletos"),
            @ApiResponse(responseCode = "409", description = "El correo electrónico ya se encuentra registrado")
    })
    // Maps HTTP POST requests on path register to this registration method
    @PostMapping("/register")
    // Registers a new user account validating payload and ensuring email uniqueness
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        // Checks whether submitted email address is already registered in database
        if (userRepository.existsByEmail(request.getEmail())) {
            // Returns HTTP 409 conflict if email address is already in use
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "El correo ya está en uso"));
        // Closes if condition checking existing email
        }

        // Retrieves default USER role from repository or creates it if absent
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.USER).build()));

        // Builds new User entity encoding password and assigning default USER role
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(userRole))
                .active(true)
                .build();

        // Persists new user account record to PostgreSQL users table
        userRepository.save(user);

        // Returns HTTP 201 created status with success message body
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Usuario registrado exitosamente"));
    // Closes registerUser handler method
    }

    /**
     * Authenticates user login credentials.
     */
    // Documents login authentication and JWT token generation endpoint in Swagger
    @Operation(summary = "Iniciar sesión", description = "Autentica credenciales en PostgreSQL contra hash BCrypt y genera el token de acceso JWT con los roles del usuario.")
    // Configures OpenAPI responses for successful login, invalid input, and bad credentials
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa, retorna token JWT y datos de sesión"),
            @ApiResponse(responseCode = "400", description = "Formato de petición inválido"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas (correo o contraseña inválidos)")
    })
    // Maps HTTP POST requests on path login to authentication handler method
    @PostMapping("/login")
    // Authenticates user credentials and issues signed JWT bearer token response
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest request) {
        // Authenticates username and password credentials against configured authentication provider
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        // Stores successful authentication token in current thread's security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Queries user entity by email to retrieve profile details and roles
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Checks whether user possesses administrator role in their assigned roles
        boolean hasAdmin = user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ADMIN);
        // Checks whether user possesses technician role in their assigned roles
        boolean hasTech = user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.TECHNICIAN);
        // Determines primary role string giving precedence to ADMIN then TECHNICIAN then USER
        String roleStr = hasAdmin ? "ADMIN" : (hasTech ? "TECHNICIAN" : "USER");

        // Generates signed JWT token incorporating user ID, names, and granted authorities
        String jwt = jwtTokenProvider.generateToken(authentication, user.getId(), user.getFirstName(), user.getLastName());

        // Returns HTTP 200 OK containing JWT token and authenticated user metadata
        return ResponseEntity.ok(JwtResponse.builder()
                .token(jwt)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role("ROLE_" + roleStr)
                .build());
    // Closes authenticateUser handler method
    }
// Closes AuthController class definition
}
