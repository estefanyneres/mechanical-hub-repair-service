package com.fiap.mechanical_hub.repair_service.application.gateways;

import com.fiap.mechanical_hub.repair_service.domain.entities.OrderTask;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * findById pede os dois IDs (nao so o do task) porque a chave no DynamoDB
 * e PK=OS#<serviceOrderId> -- sem o serviceOrderId nao tem como montar a
 * chave sem um Scan. Na pratica nunca e um problema: o path REST sempre
 * tem os dois (TASK-023: /service-orders/{orderId}/tasks/{taskId}).
 *
 * Sem findAll()/findAverageExecutionTimeByService() do monolito: listar
 * TODAS as tarefas do sistema inteiro nao e um acesso que esta tabela
 * suporta sem Scan completo, e nenhuma task da Fase 4 pede isso; a metrica
 * de tempo medio de execucao era do ReportUseCase do monolito, fora do
 * escopo desta fase.
 */
public interface OrderTaskGateway {
    OrderTask save(OrderTask orderTask);
    Optional<OrderTask> findById(UUID serviceOrderId, UUID taskId);
    List<OrderTask> findByServiceOrderId(UUID serviceOrderId);

    /**
     * Scan com filtro, nao Query -- nao existe indice por serviceId nesta
     * tabela. Aceitavel aqui: e uma checagem administrativa rara
     * (ServiceUseCase.delete), nao um caminho de alta frequencia.
     */
    boolean existsByServiceId(UUID serviceId);
}
