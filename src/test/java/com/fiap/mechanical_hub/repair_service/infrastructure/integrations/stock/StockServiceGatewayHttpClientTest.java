package com.fiap.mechanical_hub.repair_service.infrastructure.integrations.stock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do StockServiceGatewayHttpClient")
class StockServiceGatewayHttpClientTest {

    private static final String STOCK_SERVICE_URL = "http://stock-service.stock-service.svc.cluster.local";

    @Mock
    private RestTemplate restTemplate;

    private StockServiceGatewayHttpClient client;
    private UUID materialId;

    @BeforeEach
    void setUp() {
        client = new StockServiceGatewayHttpClient(restTemplate, STOCK_SERVICE_URL);
        materialId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Deve chamar o endpoint de inicialização do material")
    void initializeForMaterial_ShouldCallInitializeEndpoint() {
        client.initializeForMaterial(materialId);

        verify(restTemplate).postForLocation(
                STOCK_SERVICE_URL + "/stock/materials/" + materialId + "/initialize", null);
    }

    @Test
    @DisplayName("Deve chamar o endpoint de remoção do material")
    void deleteByMaterialId_ShouldCallDeleteEndpoint() {
        client.deleteByMaterialId(materialId);

        verify(restTemplate).delete(STOCK_SERVICE_URL + "/stock/materials/" + materialId);
    }
}
