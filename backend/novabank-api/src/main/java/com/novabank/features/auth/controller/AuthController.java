package com.novabank.features.auth.controller;

import com.novabank.features.auth.dto.LoginRequest;
import com.novabank.features.auth.dto.LoginResponse;
import com.novabank.features.auth.dto.RegisterRequest;
import com.novabank.features.auth.dto.RegisterResponse;
import com.novabank.features.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest registerRequest) {

        RegisterResponse registerResponse = authService.register(registerRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(registerResponse);
    }

}
