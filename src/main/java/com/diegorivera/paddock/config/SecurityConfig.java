package com.diegorivera.paddock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad de la aplicación.
 * Por ahora todas las páginas son públicas. La consola H2 necesita desactivar
 * la protección CSRF en su ruta y permitir que se muestre en un marco del mismo sitio.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}