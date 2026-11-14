package com.fiap.mechanical_hub.repair_service.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Catalogo (servicos/materiais) e so pra Administrador, igual ja era no
 * monolito. Tarefas de execucao (criar/listar/atualizar status) sao pra
 * Mecanico ou Administrador. Sem AuthenticationProvider/UserDetailsService:
 * a autenticacao e so leitura do token (ver SecurityFilter/JwtValidator).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity,
            SecurityFilter securityFilter) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/services/**").hasRole("ADMINISTRATOR")
                        .requestMatchers("/materials/**").hasRole("ADMINISTRATOR")
                        .requestMatchers("/service-orders/*/tasks/**").hasAnyRole("MECHANICAL", "ADMINISTRATOR")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
