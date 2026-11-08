package com.fiap.mechanical_hub.repair_service.infrastructure.integrations.stock;

import com.fiap.mechanical_hub.repair_service.application.gateways.StockServiceGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Chamada REST sincrona de verdade ao stock-service (TASK-029),
 * substituindo o stub da TASK-021.
 */
@Component
public class StockServiceGatewayHttpClient implements StockServiceGateway {

    private final RestTemplate restTemplate;
    private final String stockServiceUrl;

    public StockServiceGatewayHttpClient(RestTemplate restTemplate,
                                          @Value("${stock-service.url}") String stockServiceUrl) {
        this.restTemplate = restTemplate;
        this.stockServiceUrl = stockServiceUrl;
    }

    @Override
    public void initializeForMaterial(UUID materialId) {
        restTemplate.postForLocation(stockServiceUrl + "/stock/materials/" + materialId + "/initialize", null);
    }

    @Override
    public void deleteByMaterialId(UUID materialId) {
        restTemplate.delete(stockServiceUrl + "/stock/materials/" + materialId);
    }
}
