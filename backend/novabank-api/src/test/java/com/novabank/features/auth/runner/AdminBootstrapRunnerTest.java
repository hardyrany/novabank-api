package com.novabank.features.auth.runner;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import com.novabank.features.user.entity.User;
import com.novabank.features.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapRunnerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminBootstrapRunner runner;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(runner, "adminEmail", "admin@test.com");
        ReflectionTestUtils.setField(runner, "adminPassword", "testPassword123");
    }

    @Test
    void run_ShouldCreateAdmin_WhenNotExists() throws Exception {
        when(userRepository.existsByEmail("admin@test.com")).thenReturn(false);
        when(passwordEncoder.encode("testPassword123")).thenReturn("hashed-password");

        runner.run(null);

        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("testPassword123");
    }

    @Test
    void run_ShouldSkip_WhenAdminAlreadyExists() throws Exception {
        when(userRepository.existsByEmail("admin@test.com")).thenReturn(true);

        runner.run(null);

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }
}
