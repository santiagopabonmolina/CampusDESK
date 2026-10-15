// Defines the package namespace containing Spring Data JPA repository interfaces
package com.spring.repository;

// Imports the User entity representing system user accounts in database
import com.spring.entity.User;
// Imports RoleName enum representing distinct security authorization roles
import com.spring.entity.enums.RoleName;
// Imports JpaRepository providing standard persistence and query execution methods
import org.springframework.data.jpa.repository.JpaRepository;
// Imports Query annotation enabling custom JPQL query definition on methods
import org.springframework.data.jpa.repository.Query;
// Imports Param annotation binding named method parameters to JPQL placeholders
import org.springframework.data.repository.query.Param;
// Imports Spring stereotype repository annotation enabling component scan detection
import org.springframework.stereotype.Repository;

// Imports Java utility List collection interface for returning user lists
import java.util.List;
// Imports Optional container class supporting null-safe user lookups
import java.util.Optional;

// Marks this interface as a Spring Data managed repository component
@Repository
// Declares repository interface extending JpaRepository for User entity persistence
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds user by email.
     */
    // Queries optional user entity matching institutional email for authentication
    Optional<User> findByEmail(String email);

    /**
     * Checks email existence status.
     */
    // Checks whether an account already exists with the given email
    boolean existsByEmail(String email);

    /**
     * Finds all active users.
     */
    // Retrieves list of all active user records excluding soft-deleted accounts
    List<User> findByActiveTrue();

    /**
     * Finds active users role.
     */
    // Executes custom JPQL query filtering active users having a specified role
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.active = true")
    // Returns active users matching roleName parameter bound by Param annotation
    List<User> findByRoleNameAndActiveTrue(@Param("roleName") RoleName roleName);
// Closes the UserRepository interface definition
}
