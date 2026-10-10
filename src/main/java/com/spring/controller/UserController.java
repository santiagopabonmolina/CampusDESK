package com.spring.controller;

import com.spring.entity.User;
import com.spring.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /api/users/technicians
    // NOTA: Listo para migrar a UserResponse cuando se cree el DTO correspondiente
    @GetMapping("/technicians")
    public ResponseEntity<List<User>> getTechnicians() {
        return ResponseEntity.ok(userService.getAvailableTechnicians());
    }
}
