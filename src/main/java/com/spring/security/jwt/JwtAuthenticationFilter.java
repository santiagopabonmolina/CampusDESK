// Defines the package namespace containing JWT security components
package com.spring.security.jwt;

// Imports FilterChain enabling forwarding of requests down the filter pipeline
import jakarta.servlet.FilterChain;
// Imports ServletException thrown upon general servlet request processing failures
import jakarta.servlet.ServletException;
// Imports HttpServletRequest providing access to HTTP request headers and metadata
import jakarta.servlet.http.HttpServletRequest;
// Imports HttpServletResponse providing access to HTTP response objects
import jakarta.servlet.http.HttpServletResponse;
// Imports UsernamePasswordAuthenticationToken representing authentication credentials in Spring
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// Imports SimpleGrantedAuthority wrapping security authority strings in Spring
import org.springframework.security.core.authority.SimpleGrantedAuthority;
// Imports SecurityContextHolder providing access to the current security context
import org.springframework.security.core.context.SecurityContextHolder;
// Imports WebAuthenticationDetailsSource building request details for authentication tokens
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
// Imports Spring Component annotation declaring class as managed Spring bean
import org.springframework.stereotype.Component;
// Imports StringUtils utility class providing text checking helper methods
import org.springframework.util.StringUtils;
// Imports OncePerRequestFilter base class guaranteeing single execution per request
import org.springframework.web.filter.OncePerRequestFilter;

// Imports IOException thrown when an input or output error occurs
import java.io.IOException;
// Imports Java utility List collection interface for handling sequential lists
import java.util.List;

// Marks this class as Spring component bean for dependency injection
@Component
// Declares filter intercepting HTTP requests to validate JWT tokens and authenticate users
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Declares final JWT token provider reference for token parsing and signature validation
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Initializes JWT authentication filter.
     */
    // Explicit constructor injecting JwtTokenProvider component into filter bean
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        // Assigns injected JwtTokenProvider instance to final class field
        this.jwtTokenProvider = jwtTokenProvider;
    // Closes constructor definition
    }

    /**
     * Processes internal JWT filter.
     */
    // Overrides OncePerRequestFilter core method executing once per incoming request
    @Override
    // Filters incoming request extracting token, validating signature, and establishing security context
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Extracts raw bearer token string from HTTP Authorization header
        String token = getTokenFromRequest(request);

        // Verifies that token is non-empty and contains a valid HMAC signature
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // Extracts username email from validated token subject claim
            String email = jwtTokenProvider.getUsernameFromToken(token);
            // Extracts authorization role string from validated token claims
            String role = jwtTokenProvider.getRoleFromToken(token);

            // Wraps extracted role string into GrantedAuthority list for Spring Security
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

            // Instantiates UsernamePasswordAuthenticationToken representing authenticated principal
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);
            // Attaches web details such as remote IP address to authentication token
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Stores authenticated principal token in current thread's SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        // Closes if condition checking token validity
        }

        // Passes request and response down to the next filter in chain
        filterChain.doFilter(request, response);
    // Closes doFilterInternal method
    }

    /**
     * Extracts token from request.
     */
    // Private helper method extracting raw JWT token from Authorization header
    private String getTokenFromRequest(HttpServletRequest request) {
        // Retrieves value of HTTP Authorization request header
        String bearerToken = request.getHeader("Authorization");
        // Checks whether header has text and begins with standard Bearer prefix
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Strips leading Bearer prefix returning only the token substring
            return bearerToken.substring(7);
        // Closes if condition checking Bearer prefix
        }
        // Returns null when Authorization header is absent or improperly formatted
        return null;
    // Closes getTokenFromRequest helper method
    }
// Closes JwtAuthenticationFilter class definition
}
