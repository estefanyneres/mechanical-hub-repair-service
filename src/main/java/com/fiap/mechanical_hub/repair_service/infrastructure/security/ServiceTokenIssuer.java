package com.fiap.mechanical_hub.repair_service.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

/**
 * Este servico nunca recebe login de usuario -- so emite um token proprio,
 * de curta duracao, pra autenticar as CHAMADAS DE SAIDA que ele mesmo faz
 * ao stock-service. Mesmo issuer/secret compartilhado do TokenService do
 * core-service, pra o stock-service aceitar o token sem precisar saber de
 * onde ele veio -- so confia na assinatura e no claim "profile".
 */
@Component
public class ServiceTokenIssuer {

    private static final String SUBJECT = "repair-service";
    private static final String PROFILE = "ADMINISTRATOR";

    @Value("${api.security.jwt.secret}")
    private String secret;

    public String issue() {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withIssuer("auth-api")
                .withSubject(SUBJECT)
                .withClaim("profile", PROFILE)
                .withExpiresAt(Date.from(Instant.now().plusSeconds(60)))
                .sign(algorithm);
    }
}
