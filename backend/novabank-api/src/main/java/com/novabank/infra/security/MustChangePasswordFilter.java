package com.novabank.infra.security;

import java.io.IOException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import com.novabank.features.user.repository.UserRepository;
import com.novabank.infra.exception.ForbiddenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            if (Boolean.TRUE.equals(user.getMusChangePassword())) {
                throw new ForbiddenException(
                        "Password change required before accessing thi resource");
            }
        });

        filterChain.doFilter(request, response);
    }

    private boolean isAllowedPath(String path) {
        return path.startsWith("/api/v1/auth/change-password") || path.startsWith("/api/v1/auth/me")
                || path.startsWith("/api/v1/auth/login") || path.startsWith("/api/v1/register");
    }

}
