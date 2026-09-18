package com.novabank.features.user.service;

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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.novabank.features.user.dto.UserRequest;
import com.novabank.features.user.dto.UserResponse;
import com.novabank.features.user.entity.User;
import com.novabank.features.user.enums.Role;
import com.novabank.features.user.mapper.UserMapper;
import com.novabank.features.user.repository.UserRepository;
import com.novabank.infra.exception.BusinessException;
import com.novabank.infra.exception.ResourceNotFoundException;

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

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createUser_ShouldReturnUserResponse_WhenSuccessful() {
        // Arrange
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(userMapper.toEntity(userRequest)).thenReturn(user);
        when(passwordEncoder.encode(userRequest.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        // Act
        UserResponse result = userService.createUser(userRequest);

        // Assert
        assertNotNull(result);
        assertEquals(userResponse.getEmail(), result.getEmail());
        assertEquals(userResponse.getRoles(), result.getRoles());
        assertTrue(result.getIsActive());

        verify(userRepository).existsByEmail(userRequest.getEmail());
        verify(userMapper).toEntity(userRequest);
        verify(passwordEncoder).encode(userRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(userMapper).toResponse(any(User.class));
    }

    @Test
    void createUser_ShouldThrowBusinessException_WhenEmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessException.class, () -> userService.createUser(userRequest));

        verify(userRepository).existsByEmail(userRequest.getEmail());
        verify(userMapper, never()).toEntity(any(UserRequest.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toResponse(any(User.class));
    }

    @Test
    void createUser_ShouldEncryptPassword_WhenSuccessful() {
        // Arrange
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(userMapper.toEntity(userRequest)).thenReturn(user);
        when(passwordEncoder.encode(userRequest.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        // Act
        userService.createUser(userRequest);

        // Assert
        verify(passwordEncoder).encode("admin123");
        assertEquals(encodedPassword, user.getPassword());
    }

    @Test
    void getUserById_ShouldReturnUserResponse_WhenUserExists() {
        // Arrange
        UUID id = user.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // Act
        UserResponse result = userService.getUserById(id);

        // Assert
        assertNotNull(result);
        assertEquals(userResponse.getId(), result.getId());
        assertEquals(userResponse.getEmail(), result.getEmail());

        verify(userRepository).findById(id);
        verify(userMapper).toResponse(user);
    }

    @Test
    void getUserById_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(id));

        verify(userRepository).findById(id);
        verify(userMapper, never()).toResponse(any(User.class));
    }

}
