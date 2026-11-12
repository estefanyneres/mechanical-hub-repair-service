package com.fiap.mechanical_hub.repair_service.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceTokenIssuerTest {

    private static final String SECRET = "test-secret-shared-between-services";

    private ServiceTokenIssuer issuer;

    @BeforeEach
    void setUp() throws Exception {
        issuer = new ServiceTokenIssuer();
        Field secretField = ServiceTokenIssuer.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(issuer, SECRET);
    }

    @Test
    @DisplayName("Deve emitir um token valido, assinado com o secret compartilhado")
    void issue_ShouldGenerateVerifiableToken() {
        String token = issuer.issue();

        DecodedJWT decoded = JWT.require(Algorithm.HMAC256(SECRET))
                .withIssuer("auth-api")
                .build()
                .verify(token);

        assertThat(decoded).isNotNull();
    }

    @Test
    @DisplayName("Deve emitir o token com subject 'repair-service'")
    void issue_ShouldSetRepairServiceSubject() {
        String token = issuer.issue();

        DecodedJWT decoded = JWT.require(Algorithm.HMAC256(SECRET)).withIssuer("auth-api").build().verify(token);

        assertThat(decoded.getSubject()).isEqualTo("repair-service");
    }

    @Test
    @DisplayName("Deve emitir o token com o claim de perfil ADMINISTRATOR")
    void issue_ShouldEmbedAdministratorProfileClaim() {
        String token = issuer.issue();

        DecodedJWT decoded = JWT.require(Algorithm.HMAC256(SECRET)).withIssuer("auth-api").build().verify(token);

        assertThat(decoded.getClaim("profile").asString()).isEqualTo("ADMINISTRATOR");
    }

    @Test
    @DisplayName("Deve rejeitar o token se verificado com um secret diferente")
    void issue_ShouldBeRejected_WhenVerifiedWithDifferentSecret() {
        String token = issuer.issue();

        org.junit.jupiter.api.Assertions.assertThrows(
                com.auth0.jwt.exceptions.JWTVerificationException.class,
                () -> JWT.require(Algorithm.HMAC256("outro-secret-completamente-diferente"))
                        .withIssuer("auth-api")
                        .build()
                        .verify(token));
    }
}
