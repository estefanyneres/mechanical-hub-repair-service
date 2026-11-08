package com.fiap.mechanical_hub.repair_service.application.gateways;

import java.util.List;
import java.util.UUID;

/**
 * Chamada REST sincrona ao stock-service (TASK-029). serviceOrderId e
 * necessario nos dois metodos porque o stock-service rastreia reserva e
 * pendencia por OS, nao so por material.
 */
public interface StockReservationGateway {
    StockReservationResult reserve(UUID serviceOrderId, List<MaterialQuantity> items);

    void consume(UUID serviceOrderId, List<MaterialQuantity> items);
}
