package com.novabank.infra.security;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
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

    @Test
    @DisplayName("Should skip when there is no authentication")
    void doFilterInternal_NoAuthentication_ShouldSkip() throws Exception {
        SecurityContextHolder.clearContext();

        filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(),
                filterChain);

        verify(filterChain).doFilter(any(), any());
        verify(userRepository, never()).findByEmailIgnoreCase(any());
    }

    @Test
    @DisplayName("Should skip when authentication is anonymous")
    void doFilterInternal_AnonymousAuthentication_ShouldSkip() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("key",
                "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

        filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(),
                filterChain);

        verify(filterChain).doFilter(any(), any());
        verify(userRepository, never()).findByEmailIgnoreCase(any());
    }

    @Test
    @DisplayName("Should skip when path is in the allow-list")
    void doFilterInternal_AllowedPath_ShouldSkip() throws Exception {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("admin@novabank.local",
                        null, AuthorityUtils.createAuthorityList("ROLE_ADMIN")));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/auth/me");

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        verify(filterChain).doFilter(any(), any());
        verify(userRepository, never()).findByEmailIgnoreCase(any());
    }

}
