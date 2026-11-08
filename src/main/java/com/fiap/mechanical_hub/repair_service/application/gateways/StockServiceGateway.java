package com.fiap.mechanical_hub.repair_service.application.gateways;

import java.util.UUID;

/**
 * Placeholder deliberado pra uma futura chamada REST sincrona ao
 * stock-service (ainda nao existe -- scaffold so a partir da TASK-027).
 * O MaterialUseCase continua chamando esta interface pra manter a
 * arquitetura (o caso de uso nao sabe nem deveria saber que o outro lado
 * nao existe ainda); a implementacao de infraestrutura hoje e um stub que
 * so loga, sem nenhuma chamada de rede de verdade. Trocar pelo cliente
 * HTTP real quando o stock-service for criado.
 */
public interface StockServiceGateway {
    void initializeForMaterial(UUID materialId);
    void deleteByMaterialId(UUID materialId);
}
