// Defines the package namespace containing Spring Security configuration components
package com.spring.security.config;

// Imports JwtAuthenticationFilter validating Bearer JWT tokens on incoming requests
import com.spring.security.jwt.JwtAuthenticationFilter;
// Imports Lombok annotation generating constructor for final dependency fields
import lombok.RequiredArgsConstructor;
// Imports Spring Bean annotation declaring factory methods producing managed beans
import org.springframework.context.annotation.Bean;
// Imports Spring Configuration annotation marking class as bean configuration source
import org.springframework.context.annotation.Configuration;
// Imports AuthenticationManager interface processing authentication requests in Spring
import org.springframework.security.authentication.AuthenticationManager;
// Imports AuthenticationConfiguration exposing Spring Security authentication manager configuration
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// Imports EnableMethodSecurity enabling @PreAuthorize and @PostAuthorize method annotations
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// Imports HttpSecurity configuration builder for securing web endpoints
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// Imports EnableWebSecurity enabling Spring Security web protection integration
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// Imports SessionCreationPolicy enum for controlling HTTP session creation strategies
import org.springframework.security.config.http.SessionCreationPolicy;
// Imports BCryptPasswordEncoder implementing secure salted one-way password hashing
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// Imports PasswordEncoder interface providing password hashing and verification contract
import org.springframework.security.crypto.password.PasswordEncoder;
// Imports SecurityFilterChain interface defining matched security filters for requests
import org.springframework.security.web.SecurityFilterChain;
// Imports UsernamePasswordAuthenticationFilter standard filter class replaced by JWT
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// Imports CorsConfigurationSource configuring allowed origins, headers, and methods
import org.springframework.web.cors.CorsConfigurationSource;

// Declares this class as Spring configuration defining security beans and policies
@Configuration
// Activates web security filter chain and authorization rules for CampusDESK
@EnableWebSecurity
// Enables method-level security authorizing access via @PreAuthorize annotations
@EnableMethodSecurity
// Generates constructor injecting required final filter and CORS dependencies
@RequiredArgsConstructor
// Declares SecurityConfig establishing authentication, CORS, CSRF, and filter chain rules
public class SecurityConfig {

    // Declares final JWT authentication filter executed before standard username/password filter
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    // Declares final CORS configuration source specifying cross-origin resource sharing rules
    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * Provides BCrypt password encoder.
     */
    // Registers PasswordEncoder bean into Spring application context container
    @Bean
    // Instantiates BCryptPasswordEncoder providing adaptive slow hashing for passwords
    public PasswordEncoder passwordEncoder() {
        // Returns new BCryptPasswordEncoder instance for hashing user credentials
        return new BCryptPasswordEncoder();
    // Closes passwordEncoder bean method
    }

    /**
     * Provides authentication manager bean.
     */
    // Registers AuthenticationManager bean into Spring application context container
    @Bean
    // Exposes default AuthenticationManager configured by Spring Security framework
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        // Obtains and returns AuthenticationManager bean from configuration
        return config.getAuthenticationManager();
    // Closes authenticationManager bean method
    }

    /**
     * Configures HTTP security filter.
     */
    // Registers SecurityFilterChain bean defining URL endpoint authorization rules
    @Bean
    // Configures HttpSecurity pipeline including CORS, stateless sessions, and public endpoints
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Builds security filter chain disabling CSRF and sessions while registering JWT filter
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Builds and returns the configured SecurityFilterChain instance
        return http.build();
    // Closes securityFilterChain bean method
    }
// Closes SecurityConfig class definition
}
