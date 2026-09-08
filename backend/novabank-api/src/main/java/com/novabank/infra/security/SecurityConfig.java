package com.novabank.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(auth -> auth

                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html")
                .permitAll()

                .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()

                .requestMatchers("/api/v1/auth/**").permitAll()

                .requestMatchers("/actuator/health").permitAll()

                .requestMatchers("/login").permitAll()

                .anyRequest().authenticated())
                .formLogin(
                        form -> form.defaultSuccessUrl("/swagger-ui/index.html", true).permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2B, 12);
    }
}
