package com.fiap.mechanical_hub.repair_service.infrastructure.integrations.stock;

import com.fiap.mechanical_hub.repair_service.application.gateways.MaterialQuantity;
import com.fiap.mechanical_hub.repair_service.application.gateways.StockReservationResult;
import com.fiap.mechanical_hub.repair_service.infrastructure.security.ServiceTokenIssuer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do StockReservationGatewayHttpClient")
class StockReservationGatewayHttpClientTest {

    private static final String STOCK_SERVICE_URL = "http://stock-service.stock-service.svc.cluster.local";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ServiceTokenIssuer serviceTokenIssuer;

    private StockReservationGatewayHttpClient client;
    private UUID serviceOrderId;
    private UUID materialId;

    @BeforeEach
    void setUp() {
        lenient().when(serviceTokenIssuer.issue()).thenReturn("fake-token");
        client = new StockReservationGatewayHttpClient(restTemplate, serviceTokenIssuer, STOCK_SERVICE_URL);
        serviceOrderId = UUID.randomUUID();
        materialId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Deve retornar sucesso quando o stock-service responde reserved=true")
    void reserve_ShouldReturnSuccess_WhenServiceRespondsReserved() {
        when(restTemplate.exchange(
                eq(STOCK_SERVICE_URL + "/stock/reservations"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(StockReservationResponseDto.class)))
                .thenReturn(ResponseEntity.ok(new StockReservationResponseDto(true, null)));

        StockReservationResult result = client.reserve(serviceOrderId, List.of(new MaterialQuantity(materialId, 2)));

        assertThat(result.reserved()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar falha com o motivo quando o stock-service responde reserved=false")
    void reserve_ShouldReturnFailureWithMotivo_WhenServiceRespondsPending() {
        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(StockReservationResponseDto.class)))
                .thenReturn(ResponseEntity.ok(new StockReservationResponseDto(false, "sem peça em estoque")));

        StockReservationResult result = client.reserve(serviceOrderId, List.of(new MaterialQuantity(materialId, 2)));

        assertThat(result.reserved()).isFalse();
        assertThat(result.motivo()).isEqualTo("sem peça em estoque");
    }

    @Test
    @DisplayName("Deve retornar falha quando a resposta do stock-service vier vazia")
    void reserve_ShouldReturnFailure_WhenResponseIsNull() {
        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(StockReservationResponseDto.class)))
                .thenReturn(ResponseEntity.ok(null));

        StockReservationResult result = client.reserve(serviceOrderId, List.of(new MaterialQuantity(materialId, 2)));

        assertThat(result.reserved()).isFalse();
        assertThat(result.motivo()).isNotBlank();
    }

    @Test
    @DisplayName("Deve chamar o endpoint de consumo com o serviceOrderId e os itens")
    void consume_ShouldPostToConsumeEndpoint() {
        client.consume(serviceOrderId, List.of(new MaterialQuantity(materialId, 3)));

        ArgumentCaptor<HttpEntity<StockReservationRequestDto>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                eq(STOCK_SERVICE_URL + "/stock/reservations/consume"),
                eq(HttpMethod.POST),
                captor.capture(),
                eq(Void.class));

        StockReservationRequestDto body = captor.getValue().getBody();
        assertThat(body).isNotNull();
        assertThat(body.serviceOrderId()).isEqualTo(serviceOrderId);
        assertThat(body.items()).hasSize(1);
        assertThat(body.items().get(0).materialId()).isEqualTo(materialId);
        assertThat(body.items().get(0).quantity()).isEqualTo(3);
    }
}
