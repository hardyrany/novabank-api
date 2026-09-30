package com.novabank.features.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.novabank.features.auth.dto.ChangePasswordRequest;
import com.novabank.features.auth.dto.LoginRequest;
import com.novabank.features.auth.dto.LoginResponse;
import com.novabank.features.auth.dto.RegisterRequest;
import com.novabank.features.auth.dto.RegisterResponse;
import com.novabank.features.user.dto.UserResponse;
import com.novabank.features.user.entity.User;
import com.novabank.features.user.enums.Role;
import com.novabank.features.user.repository.UserRepository;
import com.novabank.features.user.service.UserService;
import com.novabank.infra.exception.BusinessException;
import com.novabank.infra.exception.ConflictException;
import com.novabank.infra.exception.ResourceNotFoundException;
import com.novabank.infra.exception.UnauthorizedException;

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

    @Mock
    private UserService userService;

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

        // Populate SecurityContextHolder with authenticated user
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@novabank.com", null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
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

    @Test
    void register_ShouldNotExposePasswordInResponse() {
        // Arrange
        RegisterRequest request = new RegisterRequest("newuser@novabank.com", "password123");

        when(userRepository.existsByEmail("newuser@novabank.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2b$12$hashedPassword");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RegisterResponse result = authService.register(request);

        // Assert
        assertNotNull(result);
        assertEquals("newuser@novabank.com", result.getEmail());
        assertEquals("USER", result.getRole());

        // The response class must NOT have a password field
        assertThrows(NoSuchMethodException.class,
                () -> RegisterResponse.class.getMethod("getPassword"));
    }

    @Test
    void changePassword_ShouldUpdatePassword_WhenAllValidationsPass() {

        // Arrange
        ChangePasswordRequest request =
                new ChangePasswordRequest("admin123", "newPassword456", "newPassword456");

        when(userRepository.findByEmailIgnoreCase("admin@novabank.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("admin123", user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPassword456")).thenReturn("$2b$12$newHashedPassword");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        authService.changePassword(request);

        // Assert
        verify(userRepository).findByEmailIgnoreCase("admin@novabank.com");
        verify(passwordEncoder).matches("admin123", "$2b$12$encodedPassword");
        verify(passwordEncoder).encode("newPassword456");
        verify(userRepository).save(user);

        assertEquals("$2b$12$newHashedPassword", user.getPassword());
    }

    @Test
    void changePassword_ShouldThrowBusinessException_WhenNewAndConfirmDoNotMatch() {
        // Arrange
        ChangePasswordRequest request =
                new ChangePasswordRequest("admin123", "newPassword456", "differentPassword789");

        when(userRepository.findByEmailIgnoreCase("admin@novabank.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("admin123", user.getPassword())).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessException.class, () -> authService.changePassword(request));

        verify(userRepository).findByEmailIgnoreCase("admin@novabank.com");
        verify(passwordEncoder).matches("admin123", "$2b$12$encodedPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_ShouldThrowBusinessException_WhenNewEqualsCurrent() {

        // Arrange
        ChangePasswordRequest request =
                new ChangePasswordRequest("admin123", "admin123", "admin123");

        when(userRepository.findByEmailIgnoreCase("admin@novabank.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("admin123", user.getPassword())).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessException.class, () -> authService.changePassword(request));

        verify(userRepository).findByEmailIgnoreCase("admin@novabank.com");
        verify(passwordEncoder).matches("admin123", "$2b$12$encodedPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_ShouldThrowUnauthorizedException_WhenCurrentPasswordIsIncorrect() {

        // Arrange
        ChangePasswordRequest request =
                new ChangePasswordRequest("wrongPassword", "newPassword456", "newPassword456");

        when(userRepository.findByEmailIgnoreCase("admin@novabank.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", user.getPassword())).thenReturn(false);

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> authService.changePassword(request));

        verify(userRepository).findByEmailIgnoreCase("admin@novabank.com");
        verify(passwordEncoder).matches("wrongPassword", "$2b$12$encodedPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_ShouldThrowUnauthorizedException_WhenNotAuthenticated() {

        // Arrange
        SecurityContextHolder.clearContext();

        ChangePasswordRequest request =
                new ChangePasswordRequest("admin123", "newPassword456", "newPassword456");

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> authService.changePassword(request));

        verify(userRepository, never()).findByEmailIgnoreCase(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getAuthenticatedUser_ShouldReturnUserResponse() {

        // Arrange
        UserResponse expectedResponse =
                new UserResponse(user.getId(), "admin@novabank.com", true, Set.of(Role.ADMIN));

        when(userService.getAuthenticatedUser()).thenReturn(expectedResponse);

        // Act
        UserResponse result = authService.getAuthenticatedUser();

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);

        verify(userService).getAuthenticatedUser();
    }
}
