package com.novabank.features.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.novabank.features.user.mapper.UserMapper;
import com.novabank.features.user.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper UserMapper;
    private final PasswordEncoder passwordEncoder;
    
}
