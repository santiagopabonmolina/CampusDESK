package com.spring.service;

import com.spring.entity.Role;
import com.spring.entity.User;
import com.spring.entity.enums.RoleName;
import com.spring.repository.RoleRepository;
import com.spring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Retrieves active technician users.
     */
    public List<User> getAvailableTechnicians() {
        return userRepository.findByRoleNameAndActiveTrue(RoleName.TECHNICIAN);
    }

    /**
     * Retrieves all registered users.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Updates user assigned roles.
     */
    @Transactional
    public User updateUserRoles(Long userId, Set<RoleName> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            throw new IllegalArgumentException("El usuario debe tener asignado al menos un rol");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: " + userId));

        Set<Role> newRoles = new HashSet<>();
        for (RoleName roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol especificado no existe: " + roleName));
            newRoles.add(role);
        }

        user.setRoles(newRoles);
        return userRepository.save(user);
    }
}
