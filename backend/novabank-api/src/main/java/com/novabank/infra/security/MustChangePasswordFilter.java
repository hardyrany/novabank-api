package com.novabank.infra.security;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.novabank.features.user.entity.User;
import com.novabank.features.user.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class MustChangePasswordFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public MustChangePasswordFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        if (isAllowedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = authentication.getName();

        Optional<User> userOptional = userRepository.findByEmailIgnoreCase(email);

        if (userOptional.isPresent()
                && Boolean.TRUE.equals(userOptional.get().getMustChangePassword())) {
            writeForbiddenResponse(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeForbiddenResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        String body = "{\"error\":\"Forbidden\","
                + "\"message\":\"Password change required before accessing this resource\","
                + "\"status\":403}";

        response.getWriter().write(body);
    }

    private boolean isAllowedPath(String path) {
        return path.startsWith("/api/v1/auth/change-password") || path.startsWith("/api/v1/auth/me")
                || path.startsWith("/api/v1/auth/login")
                || path.startsWith("/api/v1/auth/register");
    }
}
