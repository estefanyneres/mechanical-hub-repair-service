package com.fiap.mechanical_hub.repair_service.application.gateways;

import java.util.UUID;

/**
 * Chamada REST sincrona ao core-service (TASK-047), pra confirmar que a OS
 * esta mesmo em "Pronto para execucao" antes de aceitar INICIADO -- hoje o
 * repair-service aceitava essa transicao sem saber se o pagamento/material
 * da OS ja foi confirmado do outro lado.
 */
public interface CoreServiceGateway {
    boolean isReadyForExecution(UUID serviceOrderId);
}
