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
import org.springframework.security.crypto.password.PasswordEncoder;
import com.novabank.features.auth.dto.LoginRequest;
import com.novabank.features.auth.dto.LoginResponse;
import com.novabank.features.auth.dto.RegisterRequest;
import com.novabank.features.auth.dto.RegisterResponse;
import com.novabank.features.user.entity.User;
import com.novabank.features.user.enums.Role;
import com.novabank.features.user.repository.UserRepository;
import com.novabank.infra.exception.ConflictException;
import com.novabank.infra.exception.ResourceNotFoundException;

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

    @Mock
    private PasswordEncoder passwordEncoder;

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

    @Test
    void login_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> authService.login(loginRequest));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void login_ShouldReturnMostPrioritizedRole_WhenUserHasMultipleRoles() {
        // Arrange
        user.setRoles(Set.of(Role.ADMIN, Role.USER));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user.getEmail())).thenReturn(token);

        // Act
        LoginResponse result = authService.login(loginRequest);

        // Assert
        assertNotNull(result);
        assertEquals("ADMIN", result.getRole());

        verify(jwtService).generateToken(user.getEmail());
    }

    @Test
    void register_ShouldCreateUserWithUserRole_WhenEmailIsNew() {
        // Arrange
        RegisterRequest request = new RegisterRequest("newuser@novabank.com", "password123");

        when(userRepository.existsByEmail("newuser@novabank.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2b$12$hashedNewPassword");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RegisterResponse result = authService.register(request);

        // Assert
        assertNotNull(result);
        assertEquals("newuser@novabank.com", result.getEmail());
        assertEquals("USER", result.getRole());

        verify(userRepository).existsByEmail("newuser@novabank.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository)
                .save(argThat(savedUser -> savedUser.getRoles().equals(Set.of(Role.USER))));
    }

    @Test
    void register_ShouldThrowConflictException_WhenEmailAlreadyExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest("existing@novabank.com", "password123");

        when(userRepository.existsByEmail("existing@novabank.com")).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> authService.register(request));

        verify(userRepository).existsByEmail("existing@novabank.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}
