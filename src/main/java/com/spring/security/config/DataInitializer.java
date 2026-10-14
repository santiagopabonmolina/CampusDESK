package com.spring.security.config;

import com.spring.entity.Role;
import com.spring.entity.User;
import com.spring.entity.enums.RoleName;
import com.spring.repository.RoleRepository;
import com.spring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Inicializar roles
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ADMIN).build()));

        Role techRole = roleRepository.findByName(RoleName.TECHNICIAN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.TECHNICIAN).build()));

        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.USER).build()));

        // Asegurar que usuarios ADMIN y TECHNICIAN creados por inicialización queden VERIFICADOS
        if (!userRepository.existsByEmail("admin@campusdesk.com")) {
            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("CampusDESK")
                    .email("admin@campusdesk.com")
                    .password(passwordEncoder.encode("AdminPass2026!"))
                    .roles(Set.of(adminRole))
                    .active(true)
                    .verified(true) // Ya verificado
                    .build();
            userRepository.save(admin);
            log.info("Usuario inicial ADMIN creado con verified=true (admin@campusdesk.com / AdminPass2026!)");
        }

        if (!userRepository.existsByEmail("tecnico@campusdesk.com")) {
            User technician = User.builder()
                    .firstName("Carlos")
                    .lastName("Técnico")
                    .email("tecnico@campusdesk.com")
                    .password(passwordEncoder.encode("TechPass2026!"))
                    .roles(Set.of(techRole))
                    .active(true)
                    .verified(true) // Ya verificado
                    .build();
            userRepository.save(technician);
            log.info("Usuario inicial TECHNICIAN creado con verified=true (tecnico@campusdesk.com / TechPass2026!)");
        }
    }
}
