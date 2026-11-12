package com.fiap.mechanical_hub.repair_service.infrastructure.integrations.stock;

import com.fiap.mechanical_hub.repair_service.infrastructure.security.ServiceTokenIssuer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do StockServiceGatewayHttpClient")
class StockServiceGatewayHttpClientTest {

    private static final String STOCK_SERVICE_URL = "http://stock-service.stock-service.svc.cluster.local";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ServiceTokenIssuer serviceTokenIssuer;

    private StockServiceGatewayHttpClient client;
    private UUID materialId;

    @BeforeEach
    void setUp() {
        lenient().when(serviceTokenIssuer.issue()).thenReturn("fake-token");
        client = new StockServiceGatewayHttpClient(restTemplate, serviceTokenIssuer, STOCK_SERVICE_URL);
        materialId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Deve chamar o endpoint de inicialização do material")
    void initializeForMaterial_ShouldCallInitializeEndpoint() {
        client.initializeForMaterial(materialId);

        verify(restTemplate).exchange(
                eq(STOCK_SERVICE_URL + "/stock/materials/" + materialId + "/initialize"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class));
    }

    @Test
    @DisplayName("Deve chamar o endpoint de remoção do material")
    void deleteByMaterialId_ShouldCallDeleteEndpoint() {
        client.deleteByMaterialId(materialId);

        verify(restTemplate).exchange(
                eq(STOCK_SERVICE_URL + "/stock/materials/" + materialId),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class));
    }
}
