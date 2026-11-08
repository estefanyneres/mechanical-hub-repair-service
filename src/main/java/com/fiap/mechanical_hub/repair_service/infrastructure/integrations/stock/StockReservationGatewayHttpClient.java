package com.fiap.mechanical_hub.repair_service.infrastructure.integrations.stock;

import com.fiap.mechanical_hub.repair_service.application.gateways.MaterialQuantity;
import com.fiap.mechanical_hub.repair_service.application.gateways.StockReservationGateway;
import com.fiap.mechanical_hub.repair_service.application.gateways.StockReservationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

/**
 * Chamada REST sincrona de verdade ao stock-service (TASK-029),
 * substituindo o stub da TASK-022. Trafego interno do cluster (DNS do
 * Service, sem authorizer/JWT -- o stock-service nao exige, mesma
 * decisao documentada na TASK-027/029).
 */
@Slf4j
@Component
public class StockReservationGatewayHttpClient implements StockReservationGateway {

    private final RestTemplate restTemplate;
    private final String stockServiceUrl;

    public StockReservationGatewayHttpClient(RestTemplate restTemplate,
                                               @Value("${stock-service.url}") String stockServiceUrl) {
        this.restTemplate = restTemplate;
        this.stockServiceUrl = stockServiceUrl;
    }

    @Override
    public StockReservationResult reserve(UUID serviceOrderId, List<MaterialQuantity> items) {
        var request = toRequestDto(serviceOrderId, items);
        StockReservationResponseDto response = restTemplate.postForObject(
                stockServiceUrl + "/stock/reservations", request, StockReservationResponseDto.class);

        if (response == null) {
            log.warn("Resposta vazia do stock-service ao reservar material pra OS {}", serviceOrderId);
            return StockReservationResult.failure("Resposta vazia do stock-service");
        }

        return response.reserved() ? StockReservationResult.success() : StockReservationResult.failure(response.motivo());
    }

    @Override
    public void consume(UUID serviceOrderId, List<MaterialQuantity> items) {
        var request = toRequestDto(serviceOrderId, items);
        restTemplate.postForLocation(stockServiceUrl + "/stock/reservations/consume", request);
    }

    private StockReservationRequestDto toRequestDto(UUID serviceOrderId, List<MaterialQuantity> items) {
        List<MaterialQuantityDto> itemDtos = items.stream()
                .map(item -> new MaterialQuantityDto(item.materialId(), item.quantity()))
                .toList();
        return new StockReservationRequestDto(serviceOrderId, itemDtos);
    }
}
