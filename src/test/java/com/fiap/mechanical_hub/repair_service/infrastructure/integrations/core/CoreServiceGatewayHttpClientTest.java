package com.fiap.mechanical_hub.repair_service.infrastructure.integrations.core;

import com.fiap.mechanical_hub.repair_service.infrastructure.security.ServiceTokenIssuer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do CoreServiceGatewayHttpClient")
class CoreServiceGatewayHttpClientTest {

    private static final String CORE_SERVICE_URL = "http://core-service.core-service.svc.cluster.local";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ServiceTokenIssuer serviceTokenIssuer;

    private CoreServiceGatewayHttpClient client;
    private UUID serviceOrderId;

    @BeforeEach
    void setUp() {
        lenient().when(serviceTokenIssuer.issue()).thenReturn("fake-token");
        client = new CoreServiceGatewayHttpClient(restTemplate, serviceTokenIssuer, CORE_SERVICE_URL);
        serviceOrderId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Deve retornar true quando o core-service responde 'Pronto para execução'")
    void isReadyForExecution_ShouldReturnTrue_WhenStatusIsReadyForExecution() {
        when(restTemplate.exchange(
                eq(CORE_SERVICE_URL + "/service-orders/" + serviceOrderId + "/status"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ServiceOrderStatusResponseDto.class)))
                .thenReturn(ResponseEntity.ok(new ServiceOrderStatusResponseDto("Pronto para execução")));

        boolean result = client.isReadyForExecution(serviceOrderId);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando o core-service responde outro status")
    void isReadyForExecution_ShouldReturnFalse_WhenStatusIsAnythingElse() {
        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ServiceOrderStatusResponseDto.class)))
                .thenReturn(ResponseEntity.ok(new ServiceOrderStatusResponseDto("Pendente de estoque")));

        boolean result = client.isReadyForExecution(serviceOrderId);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando a resposta do core-service vier vazia")
    void isReadyForExecution_ShouldReturnFalse_WhenResponseIsNull() {
        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ServiceOrderStatusResponseDto.class)))
                .thenReturn(ResponseEntity.ok(null));

        boolean result = client.isReadyForExecution(serviceOrderId);

        assertThat(result).isFalse();
    }
}
