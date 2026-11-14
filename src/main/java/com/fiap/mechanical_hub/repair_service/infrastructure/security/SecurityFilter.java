package com.fiap.mechanical_hub.repair_service.infrastructure.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Autenticacao stateless, sem UserDetailsService: o perfil ja vem no
 * proprio token (claim "profile", ver JwtValidator), nenhuma consulta a
 * banco de usuarios e necessaria (este servico nao tem esse dado).
 */
@Slf4j
@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final JwtValidator jwtValidator;

    public SecurityFilter(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = recoverToken(request);
        log.warn("[DEBUG-JWT] token presente={} tamanho={}", token != null, token == null ? -1 : token.length());

        if (token != null) {
            DecodedJWT decoded = jwtValidator.decode(token);
            log.warn("[DEBUG-JWT] decoded != null: {}", decoded != null);
            if (decoded != null) {
                String profile = jwtValidator.getProfileClaim(decoded);
                log.warn("[DEBUG-JWT] profile claim={}", profile);
                if (profile != null) {
                    var authentication = new UsernamePasswordAuthenticationToken(
                            decoded.getSubject(),
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + profile.toUpperCase()))
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.warn("[DEBUG-JWT] authorities setadas={} isAuthenticated={}", authentication.getAuthorities(), authentication.isAuthenticated());
                }
            }
        }

        log.warn("[DEBUG-JWT] path={} method={} contextAuth={}", request.getRequestURI(), request.getMethod(), SecurityContextHolder.getContext().getAuthentication());
        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null) return null;
        return authHeader.replace("Bearer ", "");
    }
}
