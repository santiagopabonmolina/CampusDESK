// Defines the package namespace containing Spring Security configuration components
package com.spring.security.config;

// Imports User JPA entity representing user account records in database
import com.spring.entity.User;
// Imports UserRepository interface providing user persistence and lookup queries
import com.spring.repository.UserRepository;
// Imports Lombok annotation generating constructor for final dependency fields
import lombok.RequiredArgsConstructor;
// Imports SimpleGrantedAuthority wrapping security authority strings in Spring
import org.springframework.security.core.authority.SimpleGrantedAuthority;
// Imports UserDetails interface representing core user security credentials
import org.springframework.security.core.userdetails.UserDetails;
// Imports UserDetailsService interface for loading user-specific authentication data
import org.springframework.security.core.userdetails.UserDetailsService;
// Imports UsernameNotFoundException thrown when user cannot be located by email
import org.springframework.security.core.userdetails.UsernameNotFoundException;
// Imports Spring Service annotation declaring this class as business service component
import org.springframework.stereotype.Service;

// Imports Collectors utility class for transforming collections via streams
import java.util.stream.Collectors;

// Marks this class as Spring service component implementing UserDetailsService
@Service
// Generates constructor injecting required final UserRepository dependency automatically
@RequiredArgsConstructor
// Declares CustomUserDetailsService loading user authentication data from PostgreSQL
public class CustomUserDetailsService implements UserDetailsService {

    // Declares final repository reference for executing user database lookups
    private final UserRepository userRepository;

    /**
     * Loads user by username.
     */
    // Overrides UserDetailsService contract method locating user by email username
    @Override
    // Loads UserDetails instance by email or throws UsernameNotFoundException if absent
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Queries database for user entity by email or throws exception if not found
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        // Maps user's assigned role entities into Spring Security GrantedAuthority instances
        var authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .collect(Collectors.toSet());

        // Instantiates Spring Security UserDetails adapter with email, hash, active flag, and roles
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getActive(),
                true, true, true,
                authorities
        );
    // Closes loadUserByUsername method
    }
// Closes CustomUserDetailsService class definition
}
