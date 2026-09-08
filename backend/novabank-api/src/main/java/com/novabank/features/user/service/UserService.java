package com.novabank.features.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.novabank.features.user.dto.UserRequest;
import com.novabank.features.user.dto.UserResponse;
import com.novabank.features.user.mapper.UserMapper;
import com.novabank.features.user.repository.UserRepository;
import com.novabank.infra.exception.BusinessException;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserMapper userMapper,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createUser(UserRequest userRequest) {

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new BusinessException("Email already registered: " + userRequest.getEmail());
        }

        return null;
    }

}
