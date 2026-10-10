package com.spring.repository;

import com.spring.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // buscar por correo
    Optional<User> findByEmail(String email);

    //verificar si ya existe
    boolean existsByEmail(String email);

    //listar usuarios activos
    List<User> findByActiveTrue();

    // Listar usuarios por rol (ej: listar técnicos disponibles)
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.active = true")
    List<User> findByRoleNameAndActiveTrue(@Param("roleName") RoleName roleName);
}
}
