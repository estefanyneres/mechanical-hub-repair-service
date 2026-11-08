package com.fiap.mechanical_hub.repair_service.infrastructure.integrations.stock;

import com.fiap.mechanical_hub.repair_service.application.gateways.MaterialQuantity;
import com.fiap.mechanical_hub.repair_service.application.gateways.StockReservationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do StockReservationGatewayHttpClient")
class StockReservationGatewayHttpClientTest {

    private static final String STOCK_SERVICE_URL = "http://stock-service.stock-service.svc.cluster.local";

    @Mock
    private RestTemplate restTemplate;

    private StockReservationGatewayHttpClient client;
    private UUID serviceOrderId;
    private UUID materialId;

    @BeforeEach
    void setUp() {
        client = new StockReservationGatewayHttpClient(restTemplate, STOCK_SERVICE_URL);
        serviceOrderId = UUID.randomUUID();
        materialId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Deve retornar sucesso quando o stock-service responde reserved=true")
    void reserve_ShouldReturnSuccess_WhenServiceRespondsReserved() {
        when(restTemplate.postForObject(eq(STOCK_SERVICE_URL + "/stock/reservations"), any(), eq(StockReservationResponseDto.class)))
                .thenReturn(new StockReservationResponseDto(true, null));

        StockReservationResult result = client.reserve(serviceOrderId, List.of(new MaterialQuantity(materialId, 2)));

        assertThat(result.reserved()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar falha com o motivo quando o stock-service responde reserved=false")
    void reserve_ShouldReturnFailureWithMotivo_WhenServiceRespondsPending() {
        when(restTemplate.postForObject(any(String.class), any(), eq(StockReservationResponseDto.class)))
                .thenReturn(new StockReservationResponseDto(false, "sem peça em estoque"));

        StockReservationResult result = client.reserve(serviceOrderId, List.of(new MaterialQuantity(materialId, 2)));

        assertThat(result.reserved()).isFalse();
        assertThat(result.motivo()).isEqualTo("sem peça em estoque");
    }

    @Test
    @DisplayName("Deve retornar falha quando a resposta do stock-service vier vazia")
    void reserve_ShouldReturnFailure_WhenResponseIsNull() {
        when(restTemplate.postForObject(any(String.class), any(), eq(StockReservationResponseDto.class)))
                .thenReturn(null);

        StockReservationResult result = client.reserve(serviceOrderId, List.of(new MaterialQuantity(materialId, 2)));

        assertThat(result.reserved()).isFalse();
        assertThat(result.motivo()).isNotBlank();
    }

    @Test
    @DisplayName("Deve chamar o endpoint de consumo com o serviceOrderId e os itens")
    void consume_ShouldPostToConsumeEndpoint() {
        client.consume(serviceOrderId, List.of(new MaterialQuantity(materialId, 3)));

        ArgumentCaptor<StockReservationRequestDto> captor = ArgumentCaptor.forClass(StockReservationRequestDto.class);
        verify(restTemplate).postForLocation(eq(STOCK_SERVICE_URL + "/stock/reservations/consume"), captor.capture());

        assertThat(captor.getValue().serviceOrderId()).isEqualTo(serviceOrderId);
        assertThat(captor.getValue().items()).hasSize(1);
        assertThat(captor.getValue().items().get(0).materialId()).isEqualTo(materialId);
        assertThat(captor.getValue().items().get(0).quantity()).isEqualTo(3);
    }
}
