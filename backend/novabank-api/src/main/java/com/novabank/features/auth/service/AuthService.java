package com.novabank.features.auth.service;

import java.util.Comparator;
import java.util.Set;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Service
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService,
            UserRepository userRepository, PasswordEncoder passwordEncoder,
            UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest loginRequest) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(), loginRequest.getPassword()));

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + loginRequest.getEmail()));

        String token = jwtService.generateToken(user.getEmail());

        String role = user.getRoles().stream().min(Comparator.comparing(Enum::ordinal))
                .map(Enum::name).orElse(Role.USER.name());

        return new LoginResponse(token, user.getEmail(), role);
    }

    @Transactional
    public RegisterResponse register(RegisterRequest registerRequest) {

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ConflictException("Email already registered: " + registerRequest.getEmail());
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRoles(Set.of(Role.USER));
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        return new RegisterResponse(savedUser.getEmail(), Role.USER.name());
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new UnauthorizedException("No authenticated user found");
        }

        String email = authentication.getName();

        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(
                () -> new UnauthorizedException("User not found with email: " + email));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("New password and configuration password not match");
        }

        if (request.getNewPassword().equals(request.getCurrentPassword())) {
            throw new BusinessException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getAuthenticatedUser() {
        return userService.getAuthenticatedUser();
    }
}
