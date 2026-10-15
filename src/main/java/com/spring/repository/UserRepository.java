package com.spring.repository;

import com.spring.entity.User;
import com.spring.entity.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds user by email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks email existence status.
     */
    boolean existsByEmail(String email);

    /**
     * Finds all active users.
     */
    List<User> findByActiveTrue();

    /**
     * Finds active users role.
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.active = true")
    List<User> findByRoleNameAndActiveTrue(@Param("roleName") RoleName roleName);
}
