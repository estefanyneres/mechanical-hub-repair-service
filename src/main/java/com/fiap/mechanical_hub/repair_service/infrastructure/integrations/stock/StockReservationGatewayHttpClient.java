package com.fiap.mechanical_hub.repair_service.infrastructure.integrations.stock;

import com.fiap.mechanical_hub.repair_service.application.gateways.MaterialQuantity;
import com.fiap.mechanical_hub.repair_service.application.gateways.StockReservationGateway;
import com.fiap.mechanical_hub.repair_service.application.gateways.StockReservationResult;
import com.fiap.mechanical_hub.repair_service.infrastructure.security.ServiceTokenIssuer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

/**
 * Chamada REST sincrona de verdade ao stock-service (TASK-029), com token
 * de servico proprio (ServiceTokenIssuer) -- o stock-service agora exige
 * JWT com perfil ADMINISTRATOR, igual era /stock/** no monolito.
 */
@Slf4j
@Component
public class StockReservationGatewayHttpClient implements StockReservationGateway {

    private final RestTemplate restTemplate;
    private final ServiceTokenIssuer serviceTokenIssuer;
    private final String stockServiceUrl;

    public StockReservationGatewayHttpClient(RestTemplate restTemplate,
                                               ServiceTokenIssuer serviceTokenIssuer,
                                               @Value("${stock-service.url}") String stockServiceUrl) {
        this.restTemplate = restTemplate;
        this.serviceTokenIssuer = serviceTokenIssuer;
        this.stockServiceUrl = stockServiceUrl;
    }

    @Override
    public StockReservationResult reserve(UUID serviceOrderId, List<MaterialQuantity> items) {
        var request = toRequestDto(serviceOrderId, items);
        var response = restTemplate.exchange(
                stockServiceUrl + "/stock/reservations",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders()),
                StockReservationResponseDto.class);

        StockReservationResponseDto body = response.getBody();
        if (body == null) {
            log.warn("Resposta vazia do stock-service ao reservar material pra OS {}", serviceOrderId);
            return StockReservationResult.failure("Resposta vazia do stock-service");
        }

        return body.reserved() ? StockReservationResult.success() : StockReservationResult.failure(body.motivo());
    }

    @Override
    public void consume(UUID serviceOrderId, List<MaterialQuantity> items) {
        var request = toRequestDto(serviceOrderId, items);
        restTemplate.exchange(
                stockServiceUrl + "/stock/reservations/consume",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders()),
                Void.class);
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceTokenIssuer.issue());
        return headers;
    }

    private StockReservationRequestDto toRequestDto(UUID serviceOrderId, List<MaterialQuantity> items) {
        List<MaterialQuantityDto> itemDtos = items.stream()
                .map(item -> new MaterialQuantityDto(item.materialId(), item.quantity()))
                .toList();
        return new StockReservationRequestDto(serviceOrderId, itemDtos);
    }
}
