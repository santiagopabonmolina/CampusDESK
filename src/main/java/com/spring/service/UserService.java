package com.spring.service;

import com.spring.entity.User;
import com.spring.entity.enums.RoleName;
import com.spring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAvailableTechnicians() {
        return userRepository.findByRoleNameAndActiveTrue(RoleName.TECHNICIAN);
    }
}
