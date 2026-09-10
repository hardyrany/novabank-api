package com.novabank.features.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.novabank.features.auth.dto.LoginRequest;
import com.novabank.features.auth.dto.LoginResponse;
import com.novabank.features.user.entity.User;
import com.novabank.features.user.enums.Role;
import com.novabank.features.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private LoginRequest loginRequest;
    private User user;
    private String token;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("admin@novabank.com", "admin123");

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@novabank.com");
        user.setPassword("$2b$12$encodedPassword");
        user.setIsActive(true);
        user.setRoles(Set.of(Role.ADMIN));

        token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBub3ZhYmFuay5jb20ifQ.abc123";
    }

    @Test
    void login_ShouldReturnLoginResponse_WhenSuccessful() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user.getEmail())).thenReturn(token);

        // Act
        LoginResponse result = authService.login(loginRequest);

        // Assert
        assertNotNull(result);
        assertEquals(token, result.getToken());
        assertEquals("admin@novabank.com", result.getEmail());
        assertEquals("ADMIN", result.getRole());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(jwtService).generateToken(user.getEmail());
    }

    @Test
    void login_ShouldThrowBadCredentialsException_WhenCredentialsInvalid() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generateToken(anyString());
    }

}
