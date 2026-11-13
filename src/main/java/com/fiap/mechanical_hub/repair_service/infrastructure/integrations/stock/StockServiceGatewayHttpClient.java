package com.fiap.mechanical_hub.repair_service.infrastructure.integrations.stock;

import com.fiap.mechanical_hub.repair_service.application.gateways.StockServiceGateway;
import com.fiap.mechanical_hub.repair_service.infrastructure.security.ServiceTokenIssuer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Chamada REST sincrona de verdade ao stock-service (TASK-029), com token
 * de servico proprio (ServiceTokenIssuer) -- o stock-service agora exige
 * JWT com perfil ADMINISTRATOR, igual era /stock/** no monolito.
 */
@Component
public class StockServiceGatewayHttpClient implements StockServiceGateway {

    private final RestTemplate restTemplate;
    private final ServiceTokenIssuer serviceTokenIssuer;
    private final String stockServiceUrl;

    public StockServiceGatewayHttpClient(RestTemplate restTemplate,
                                          ServiceTokenIssuer serviceTokenIssuer,
                                          @Value("${stock-service.url}") String stockServiceUrl) {
        this.restTemplate = restTemplate;
        this.serviceTokenIssuer = serviceTokenIssuer;
        this.stockServiceUrl = stockServiceUrl;
    }

    @Override
    public void initializeForMaterial(UUID materialId) {
        restTemplate.exchange(
                stockServiceUrl + "/stock/materials/" + materialId + "/initialize",
                HttpMethod.POST,
                new HttpEntity<>(null, authHeaders()),
                Void.class);
    }

    @Override
    public void deleteByMaterialId(UUID materialId) {
        restTemplate.exchange(
                stockServiceUrl + "/stock/materials/" + materialId,
                HttpMethod.DELETE,
                new HttpEntity<>(null, authHeaders()),
                Void.class);
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceTokenIssuer.issue());
        return headers;
    }
}
