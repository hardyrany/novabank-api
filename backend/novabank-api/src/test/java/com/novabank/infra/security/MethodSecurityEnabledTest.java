package com.novabank.infra.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MethodSecurityEnabledTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void securityConfigShouldHaveEnableMethodSecurityAnnotation() {
        boolean hasAnnotation =
                SecurityConfig.class.isAnnotationPresent(EnableMethodSecurity.class);

        assertThat(hasAnnotation).as("SecurityConfig must be annotated with @EnableMethodSecurity")
                .isTrue();
    }
}
