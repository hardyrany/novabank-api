package com.novabank.features.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.novabank.features.user.dto.UserRequest;
import com.novabank.features.user.dto.UserResponse;
import com.novabank.features.user.entity.User;
import com.novabank.features.user.enums.Role;
import com.novabank.features.user.mapper.UserMapper;
import com.novabank.features.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRequest userRequest;
    private User user;
    private UserResponse userResponse;
    private String encodedPassword;

    @BeforeEach
    void setUp() {
        userRequest = new UserRequest("admin@novabank.com", "admin123", Set.of(Role.ADMIN));

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@novabank.com");
        user.setPassword("admin123");
        user.setIsActive(true);
        user.setRoles(Set.of(Role.ADMIN));

        encodedPassword = "$2b$12$encodedPasswordHash";
        userResponse =
                new UserResponse(user.getId(), "admin@novabank.com", true, Set.of(Role.ADMIN));
    }

}
