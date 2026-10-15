package com.spring.repository;

import com.spring.entity.Role;
import com.spring.entity.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Finds role by name.
     */
    Optional<Role> findByName(RoleName name);
}
