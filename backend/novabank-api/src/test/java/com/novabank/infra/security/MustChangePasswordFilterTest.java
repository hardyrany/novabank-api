package com.novabank.infra.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
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
import com.novabank.features.user.entity.User;
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

    @Test
    @DisplayName("Should block when mustChangePassword is true and path is not allowed")
    void doFilterInternal_FlagTrue_BlockedPath_ShouldReturn403() throws Exception {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("admin@novabank.local",
                        null, AuthorityUtils.createAuthorityList("ROLE_ADMIN")));

        User user = new User();
        user.setMustChangePassword(true);
        when(userRepository.findByEmailIgnoreCase("admin@novabank.local"))
                .thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/accounts/1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("Password change required");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Should pass when mustChangePassword is false")
    void doFilterInternal_FlagFalse_ShouldPass() throws Exception {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("admin@novabank.local",
                        null, AuthorityUtils.createAuthorityList("ROLE_ADMIN")));

        User user = new User();
        user.setMustChangePassword(false);
        when(userRepository.findByEmailIgnoreCase("admin@novabank.local"))
                .thenReturn(Optional.of(user));

        filter.doFilterInternal(new MockHttpServletRequest("GET", "/api/v1/accounts/1"),
                new MockHttpServletResponse(), filterChain);

        verify(filterChain).doFilter(any(), any());
    }

    @Test
    @DisplayName("Should pass when user is not found (defensive)")
    void doFilterInternal_UserNotFound_ShouldPass() throws Exception {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("ghost@novabank.local",
                        null, AuthorityUtils.createAuthorityList("ROLE_USER")));

        when(userRepository.findByEmailIgnoreCase("ghost@novabank.local"))
                .thenReturn(Optional.empty());

        filter.doFilterInternal(new MockHttpServletRequest("GET", "/api/v1/accounts/1"),
                new MockHttpServletResponse(), filterChain);

        verify(filterChain).doFilter(any(), any());
    }

}
