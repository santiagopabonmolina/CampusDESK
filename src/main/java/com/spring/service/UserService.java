// Defines the package namespace containing business service layer classes
package com.spring.service;

// Imports Role JPA entity representing security authorization role records
import com.spring.entity.Role;
// Imports User JPA entity representing user account records
import com.spring.entity.User;
// Imports RoleName enum representing supported system authorization roles
import com.spring.entity.enums.RoleName;
// Imports RoleRepository interface providing role database operations
import com.spring.repository.RoleRepository;
// Imports UserRepository interface providing user database operations
import com.spring.repository.UserRepository;
// Imports Lombok annotation generating constructor for final dependency fields
import lombok.RequiredArgsConstructor;
// Imports HttpStatus enum representing standard HTTP status response codes
import org.springframework.http.HttpStatus;
// Imports Spring Service annotation declaring this class as business service component
import org.springframework.stereotype.Service;
// Imports Spring Transactional annotation for managing transactional database boundaries
import org.springframework.transaction.annotation.Transactional;
// Imports ResponseStatusException for throwing HTTP-aware exceptions from services
import org.springframework.web.server.ResponseStatusException;

// Imports HashSet class for collecting unique role entity instances
import java.util.HashSet;
// Imports List interface representing an ordered collection of objects
import java.util.List;
// Imports Set interface representing an unordered collection of unique elements
import java.util.Set;

// Marks this class as Spring service component for user account operations
@Service
// Generates constructor injecting required final repository dependencies automatically
@RequiredArgsConstructor
// Declares UserService handling user query and role assignment business workflows
public class UserService {

    // Declares final repository reference for executing user database persistence operations
    private final UserRepository userRepository;
    // Declares final repository reference for looking up system role entities
    private final RoleRepository roleRepository;

    /**
     * Retrieves active technician users.
     */
    // Queries all active user accounts possessing the TECHNICIAN role authority
    public List<User> getAvailableTechnicians() {
        // Retrieves active technician list from database for ticket assignment selection
        return userRepository.findByRoleNameAndActiveTrue(RoleName.TECHNICIAN);
    // Closes getAvailableTechnicians method
    }

    /**
     * Retrieves all registered users.
     */
    // Queries and returns complete list of registered system user accounts
    public List<User> getAllUsers() {
        // Queries all user records from database table via repository
        return userRepository.findAll();
    // Closes getAllUsers method
    }

    /**
     * Updates user assigned roles.
     */
    // Manages database transaction boundary ensuring atomic role updating operations
    @Transactional
    // Updates role assignments for a user validating non-empty roles and existence
    public User updateUserRoles(Long userId, Set<RoleName> roleNames) {
        // Verifies that assigned role names set is neither null nor empty
        if (roleNames == null || roleNames.isEmpty()) {
            // Throws IllegalArgumentException requiring at least one assigned role
            throw new IllegalArgumentException("El usuario debe tener asignado al menos un rol");
        // Closes if condition checking roleNames emptiness
        }

        // Queries user entity by identifier or throws HTTP 404 if not found
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: " + userId));

        // Initializes set to accumulate resolved Role entity database instances
        Set<Role> newRoles = new HashSet<>();
        // Iterates through each RoleName enum provided in the update set
        for (RoleName roleName : roleNames) {
            // Looks up Role entity by enum name or throws HTTP 400 bad request
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol especificado no existe: " + roleName));
            // Adds resolved Role entity to the new roles collection set
            newRoles.add(role);
        // Closes for loop iterating roleNames
        }

        // Replaces user's assigned roles with newly resolved roles collection
        user.setRoles(newRoles);
        // Persists updated user entity to database and returns saved instance
        return userRepository.save(user);
    // Closes updateUserRoles method
    }
// Closes UserService class definition
}
