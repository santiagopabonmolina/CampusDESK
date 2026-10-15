// Defines the package namespace containing Spring Data JPA repository interfaces
package com.spring.repository;

// Imports the Role entity managed by this repository
import com.spring.entity.Role;
// Imports RoleName enum representing supported user roles
import com.spring.entity.enums.RoleName;
// Imports JpaRepository providing standard database persistence functionality
import org.springframework.data.jpa.repository.JpaRepository;
// Imports Spring stereotype repository annotation for automatic persistence bean creation
import org.springframework.stereotype.Repository;

// Imports Optional container class for null-safe value retrieval
import java.util.Optional;

// Marks this interface as a Spring Data managed repository component
@Repository
// Declares repository interface extending JpaRepository for Role entity persistence
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Finds role by name.
     */
    // Queries the database to find an optional role by its enum name
    Optional<Role> findByName(RoleName name);
// Closes the RoleRepository interface definition
}
