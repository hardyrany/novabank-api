package com.novabank.infra.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import com.novabank.features.user.repository.UserRepository;

import jakarta.servlet.FilterChain;

@ExtendWith(MockitoExtension.class)
class MustChangePasswordFilterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    private MustChangePasswordFilter filter;

    @BeforeEach
    void setUp() {
        filter = new MustChangePasswordFilter(userRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }


}
