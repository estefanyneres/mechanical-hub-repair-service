package com.fiap.mechanical_hub.repair_service.application.usecases;

import com.fiap.mechanical_hub.repair_service.application.dto.ordertask.CreateOrderTaskRequest;
import com.fiap.mechanical_hub.repair_service.application.dto.ordertask.MaterialQuantityRequest;
import com.fiap.mechanical_hub.repair_service.application.dto.ordertask.OrderTaskResponse;
import com.fiap.mechanical_hub.repair_service.application.gateways.CoreServiceGateway;
import com.fiap.mechanical_hub.repair_service.application.gateways.MaterialQuantity;
import com.fiap.mechanical_hub.repair_service.application.gateways.OrderTaskGateway;
import com.fiap.mechanical_hub.repair_service.application.gateways.StockReservationGateway;
import com.fiap.mechanical_hub.repair_service.application.presenters.OrderTaskPresenter;
import com.fiap.mechanical_hub.repair_service.domain.entities.Material;
import com.fiap.mechanical_hub.repair_service.domain.entities.MaterialRequirement;
import com.fiap.mechanical_hub.repair_service.domain.entities.OrderTask;
import com.fiap.mechanical_hub.repair_service.domain.entities.OrderTaskMaterialSnapshot;
import com.fiap.mechanical_hub.repair_service.domain.entities.ServiceData;
import com.fiap.mechanical_hub.repair_service.domain.enums.TaskStatusEnum;
import com.fiap.mechanical_hub.repair_service.domain.exceptions.BusinessRuleException;
import com.fiap.mechanical_hub.repair_service.domain.exceptions.NotFoundException;
import com.fiap.mechanical_hub.repair_service.infrastructure.messaging.SagaEventPublisher;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

/**
 * Criacao de tarefa NAO toca estoque nem orcamento (RFC-001 ja mudou isso:
 * reserva de material so acontece depois do pagamento confirmado -
 * TASK-022; orcamento e calculado pelo billing-service, que vai consultar
 * findByServiceOrderId pra saber o que foi selecionado). Atualizacao de
 * status tambem nao reconfirma material reservado por tarefa -- isso ja
 * foi confirmado uma vez so, pra OS inteira, antes da execucao comecar.
 */
@Slf4j
public class OrderTaskUseCase {

    private final OrderTaskGateway orderTaskGateway;
    private final ServiceUseCase serviceUseCase;
    private final MaterialUseCase materialUseCase;
    private final OrderTaskPresenter presenter;
    private final SagaEventPublisher publisher;
    private final StockReservationGateway stockReservationGateway;
    private final CoreServiceGateway coreServiceGateway;

    public OrderTaskUseCase(OrderTaskGateway orderTaskGateway, ServiceUseCase serviceUseCase,
                             MaterialUseCase materialUseCase, OrderTaskPresenter presenter,
                             SagaEventPublisher publisher, StockReservationGateway stockReservationGateway,
                             CoreServiceGateway coreServiceGateway) {
        this.orderTaskGateway = orderTaskGateway;
        this.serviceUseCase = serviceUseCase;
        this.materialUseCase = materialUseCase;
        this.presenter = presenter;
        this.publisher = publisher;
        this.stockReservationGateway = stockReservationGateway;
        this.coreServiceGateway = coreServiceGateway;
    }

    public OrderTaskResponse create(UUID serviceOrderId, CreateOrderTaskRequest request) {
        boolean duplicated = orderTaskGateway.findByServiceOrderId(serviceOrderId).stream()
                .anyMatch(task -> task.getServiceData().getId().equals(request.serviceId()));
        if (duplicated) {
            throw new BusinessRuleException("Este serviço já foi adicionado a esta ordem");
        }

        ServiceData serviceData = serviceUseCase.findServiceById(request.serviceId());

        List<OrderTaskMaterialSnapshot> materials = (request.materials() != null && !request.materials().isEmpty())
                ? resolveFromRequest(request.materials())
                : resolveFromCatalogDefaults(serviceData.getDefaultMaterials());

        OrderTask task = OrderTask.create(serviceOrderId, serviceData, materials);
        OrderTask saved = orderTaskGateway.save(task);

        return presenter.toResponse(saved);
    }

    public List<OrderTaskResponse> findByServiceOrderId(UUID serviceOrderId) {
        return presenter.toResponseList(orderTaskGateway.findByServiceOrderId(serviceOrderId));
    }

    public void updateStatus(UUID serviceOrderId, UUID taskId, String status) {
        TaskStatusEnum targetStatus = TaskStatusEnum.fromString(status);
        OrderTask task = orderTaskGateway.findById(serviceOrderId, taskId)
                .orElseThrow(() -> new NotFoundException("Tarefa não encontrada com ID: " + taskId));

        switch (targetStatus) {
            case INICIADO -> {
                validateOrderReadyForExecution(serviceOrderId);
                task.start();
                publishIfFirstTaskStarted(serviceOrderId, task.getId());
            }
            case FINALIZADO -> {
                task.finish();
                consumeTaskMaterials(serviceOrderId, task);
            }
            default -> throw new BusinessRuleException("Transição de status não permitida: " + status);
        }

        orderTaskGateway.save(task);

        if (targetStatus == TaskStatusEnum.FINALIZADO) {
            publishIfOrderFullyFinished(serviceOrderId);
        }
    }

    /**
     * TASK-047: antes, o repair-service aceitava INICIADO sem saber se a OS
     * tinha pagamento/material confirmados do outro lado -- so descobria
     * que algo estava errado depois, quando execucao.finalizada chegava no
     * core-service e era rejeitada (ver TASK-046). Agora confirma de forma
     * sincrona ANTES de aceitar.
     */
    private void validateOrderReadyForExecution(UUID serviceOrderId) {
        if (!coreServiceGateway.isReadyForExecution(serviceOrderId)) {
            throw new BusinessRuleException(
                    "Não é possível iniciar a execução: a ordem de serviço ainda não está pronta para execução");
        }
    }

    /**
     * Publica execucao.iniciada so quando NENHUMA outra tarefa da OS ja
     * saiu de PENDENTE -- o core-service so quer saber do primeiro
     * INICIADO (TASK-048), nao de cada tarefa individual.
     */
    private void publishIfFirstTaskStarted(UUID serviceOrderId, UUID currentTaskId) {
        boolean alreadyStarted = orderTaskGateway.findByServiceOrderId(serviceOrderId).stream()
                .anyMatch(t -> !t.getId().equals(currentTaskId) && t.getStatus() != TaskStatusEnum.PENDENTE);

        if (!alreadyStarted) {
            publisher.publishExecucaoIniciada(serviceOrderId);
            log.info("Primeira tarefa da OS {} iniciada - execucao.iniciada publicado", serviceOrderId);
        }
    }

    /**
     * Baixa do material reservado pra ESTA tarefa especifica, no momento
     * em que ela termina -- nao espera a OS inteira finalizar (mesmo
     * comportamento por-tarefa do monolito original).
     */
    private void consumeTaskMaterials(UUID serviceOrderId, OrderTask task) {
        if (task.getMaterials() == null || task.getMaterials().isEmpty()) {
            return;
        }

        List<MaterialQuantity> items = task.getMaterials().stream()
                .map(material -> new MaterialQuantity(material.materialId(), material.quantity()))
                .toList();

        stockReservationGateway.consume(serviceOrderId, items);
    }

    /**
     * Falha durante a execucao manual ja iniciada (nao e sobre falta de
     * material -- isso ja e reportado antes, via material.indisponivel).
     * So retransmite o aviso pro orquestrador do Saga; nao muda o status
     * da tarefa aqui, quem decide o que fazer com a OS e o core-service
     * (volta pra EM_DIAGNOSTICO - RevertToDiagnosisTransition).
     */
    public void reportExecutionFailure(UUID serviceOrderId, String motivo) {
        publisher.publishExecucaoFalhou(serviceOrderId, motivo);
        log.warn("Falha reportada na execução da OS {}: {}", serviceOrderId, motivo);
    }

    private void publishIfOrderFullyFinished(UUID serviceOrderId) {
        List<OrderTask> tasks = orderTaskGateway.findByServiceOrderId(serviceOrderId);
        boolean allFinished = !tasks.isEmpty() && tasks.stream().allMatch(OrderTask::isFinished);
        if (allFinished) {
            publisher.publishExecucaoFinalizada(serviceOrderId);
            log.info("Todas as tarefas da OS {} concluídas - execucao.finalizada publicado", serviceOrderId);
        }
    }

    private List<OrderTaskMaterialSnapshot> resolveFromRequest(List<MaterialQuantityRequest> items) {
        return items.stream()
                .map(item -> {
                    Material material = materialUseCase.findById(item.materialId());
                    return new OrderTaskMaterialSnapshot(material.getId(), material.getName(), item.quantity());
                })
                .toList();
    }

    private List<OrderTaskMaterialSnapshot> resolveFromCatalogDefaults(List<MaterialRequirement> defaults) {
        return defaults.stream()
                .map(requirement -> {
                    Material material = materialUseCase.findById(requirement.materialId());
                    return new OrderTaskMaterialSnapshot(material.getId(), material.getName(), requirement.quantity());
                })
                .toList();
    }
}
