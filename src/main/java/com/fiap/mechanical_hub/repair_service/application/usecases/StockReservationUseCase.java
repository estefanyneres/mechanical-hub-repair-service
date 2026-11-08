package com.fiap.mechanical_hub.repair_service.application.usecases;

import com.fiap.mechanical_hub.repair_service.application.gateways.MaterialQuantity;
import com.fiap.mechanical_hub.repair_service.application.gateways.OrderTaskGateway;
import com.fiap.mechanical_hub.repair_service.application.gateways.StockReservationGateway;
import com.fiap.mechanical_hub.repair_service.application.gateways.StockReservationResult;
import com.fiap.mechanical_hub.repair_service.domain.entities.OrderTask;
import com.fiap.mechanical_hub.repair_service.domain.entities.OrderTaskMaterialSnapshot;
import com.fiap.mechanical_hub.repair_service.infrastructure.messaging.SagaEventPublisher;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Disparada tanto por pagamento.confirmado (primeira tentativa) quanto por
 * estoque.reabastecido (nova tentativa) -- as duas vezes tenta reservar de
 * novo, nunca assume que uma reserva anterior ainda vale. material.reservado
 * e o unico evento que o core-service aceita pra avancar pra
 * PRONTO_PARA_EXECUCAO, venha ele de qual gatilho for.
 */
@Slf4j
public class StockReservationUseCase {

    private final OrderTaskGateway orderTaskGateway;
    private final StockReservationGateway stockReservationGateway;
    private final SagaEventPublisher publisher;

    public StockReservationUseCase(OrderTaskGateway orderTaskGateway, StockReservationGateway stockReservationGateway,
                                    SagaEventPublisher publisher) {
        this.orderTaskGateway = orderTaskGateway;
        this.stockReservationGateway = stockReservationGateway;
        this.publisher = publisher;
    }

    public void reserveForOrder(UUID serviceOrderId) {
        List<OrderTask> tasks = orderTaskGateway.findByServiceOrderId(serviceOrderId);
        List<MaterialQuantity> items = aggregateMaterials(tasks);

        StockReservationResult result = stockReservationGateway.reserve(serviceOrderId, items);

        if (result.reserved()) {
            publisher.publishMaterialReservado(serviceOrderId);
            log.info("Material reservado pra OS {} ({} item(ns))", serviceOrderId, items.size());
        } else {
            publisher.publishMaterialIndisponivel(serviceOrderId, result.motivo());
            log.warn("Material indisponível pra OS {}: {}", serviceOrderId, result.motivo());
        }
    }

    private List<MaterialQuantity> aggregateMaterials(List<OrderTask> tasks) {
        Map<UUID, Integer> totalByMaterial = new LinkedHashMap<>();
        for (OrderTask task : tasks) {
            for (OrderTaskMaterialSnapshot material : task.getMaterials()) {
                totalByMaterial.merge(material.materialId(), material.quantity(), Integer::sum);
            }
        }
        return totalByMaterial.entrySet().stream()
                .map(entry -> new MaterialQuantity(entry.getKey(), entry.getValue()))
                .toList();
    }
}
