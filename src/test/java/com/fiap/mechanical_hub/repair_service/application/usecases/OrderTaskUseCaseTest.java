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
import com.fiap.mechanical_hub.repair_service.domain.exceptions.BusinessRuleException;
import com.fiap.mechanical_hub.repair_service.domain.exceptions.NotFoundException;
import com.fiap.mechanical_hub.repair_service.infrastructure.messaging.SagaEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.fiap.mechanical_hub.repair_service.domain.entities.mocks.MaterialMock.defaultMaterial;
import static com.fiap.mechanical_hub.repair_service.domain.entities.mocks.ServiceMock.defaultService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderTaskUseCaseTest {

    @Mock
    private OrderTaskGateway orderTaskGateway;
    @Mock
    private ServiceUseCase serviceUseCase;
    @Mock
    private MaterialUseCase materialUseCase;
    @Mock
    private OrderTaskPresenter presenter;
    @Mock
    private SagaEventPublisher publisher;
    @Mock
    private StockReservationGateway stockReservationGateway;
    @Mock
    private CoreServiceGateway coreServiceGateway;

    private OrderTaskUseCase useCase;
    private UUID serviceOrderId;

    @BeforeEach
    void setUp() {
        useCase = new OrderTaskUseCase(orderTaskGateway, serviceUseCase, materialUseCase, presenter, publisher,
                stockReservationGateway, coreServiceGateway);
        serviceOrderId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Deve criar tarefa usando os materiais padrão do catálogo quando nenhum for informado")
    void create_ShouldUseCatalogDefaultMaterials_WhenNoneProvided() {
        Material material = defaultMaterial();
        ServiceData service = defaultService();
        service.setDefaultMaterials(List.of(new MaterialRequirement(material.getId(), 2)));

        var request = new CreateOrderTaskRequest(service.getId(), null);

        when(orderTaskGateway.findByServiceOrderId(serviceOrderId)).thenReturn(List.of());
        when(serviceUseCase.findServiceById(service.getId())).thenReturn(service);
        when(materialUseCase.findById(material.getId())).thenReturn(material);
        when(orderTaskGateway.save(any(OrderTask.class))).thenAnswer(inv -> inv.getArgument(0));
        when(presenter.toResponse(any(OrderTask.class))).thenReturn(mock(OrderTaskResponse.class));

        useCase.create(serviceOrderId, request);

        verify(orderTaskGateway).save(argThat(task ->
                task.getMaterials().size() == 1
                        && task.getMaterials().get(0).materialId().equals(material.getId())
                        && task.getMaterials().get(0).quantity() == 2));
    }

    @Test
    @DisplayName("Deve criar tarefa usando os materiais explícitos informados, sobrepondo o catálogo")
    void create_ShouldUseExplicitMaterials_WhenProvided() {
        Material material = defaultMaterial();
        ServiceData service = defaultService();
        var request = new CreateOrderTaskRequest(service.getId(),
                List.of(new MaterialQuantityRequest(material.getId(), 5)));

        when(orderTaskGateway.findByServiceOrderId(serviceOrderId)).thenReturn(List.of());
        when(serviceUseCase.findServiceById(service.getId())).thenReturn(service);
        when(materialUseCase.findById(material.getId())).thenReturn(material);
        when(orderTaskGateway.save(any(OrderTask.class))).thenAnswer(inv -> inv.getArgument(0));
        when(presenter.toResponse(any(OrderTask.class))).thenReturn(mock(OrderTaskResponse.class));

        useCase.create(serviceOrderId, request);

        verify(orderTaskGateway).save(argThat(task -> task.getMaterials().get(0).quantity() == 5));
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException se o serviço já foi adicionado a esta OS")
    void create_ShouldThrow_WhenServiceAlreadyAddedToOrder() {
        ServiceData service = defaultService();
        OrderTask existingTask = OrderTask.create(serviceOrderId, service, List.of());
        var request = new CreateOrderTaskRequest(service.getId(), null);

        when(orderTaskGateway.findByServiceOrderId(serviceOrderId)).thenReturn(List.of(existingTask));

        assertThatThrownBy(() -> useCase.create(serviceOrderId, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("já foi adicionado");

        verify(orderTaskGateway, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar as tarefas de uma ordem")
    void findByServiceOrderId_ShouldReturnList() {
        when(orderTaskGateway.findByServiceOrderId(serviceOrderId)).thenReturn(List.of());
        when(presenter.toResponseList(List.of())).thenReturn(List.of());

        var result = useCase.findByServiceOrderId(serviceOrderId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve publicar execucao.iniciada quando a primeira tarefa da OS é iniciada")
    void updateStatus_ShouldPublishExecucaoIniciada_WhenFirstTaskStarts() {
        ServiceData service = defaultService();
        OrderTask task = OrderTask.create(serviceOrderId, service, List.of());
        UUID taskId = task.getId();

        when(coreServiceGateway.isReadyForExecution(serviceOrderId)).thenReturn(true);
        when(orderTaskGateway.findById(serviceOrderId, taskId)).thenReturn(Optional.of(task));
        when(orderTaskGateway.findByServiceOrderId(serviceOrderId)).thenReturn(List.of(task));

        useCase.updateStatus(serviceOrderId, taskId, "INICIADO");

        verify(orderTaskGateway).save(task);
        verify(publisher).publishExecucaoIniciada(serviceOrderId);
    }

    @Test
    @DisplayName("Não deve publicar execucao.iniciada se outra tarefa da OS já tinha sido iniciada antes")
    void updateStatus_ShouldNotPublishExecucaoIniciada_WhenAnotherTaskAlreadyStarted() {
        ServiceData service = defaultService();
        OrderTask alreadyStartedTask = OrderTask.create(serviceOrderId, defaultService(), List.of());
        alreadyStartedTask.start();

        OrderTask taskToStart = OrderTask.create(serviceOrderId, service, List.of());
        UUID taskId = taskToStart.getId();

        when(coreServiceGateway.isReadyForExecution(serviceOrderId)).thenReturn(true);
        when(orderTaskGateway.findById(serviceOrderId, taskId)).thenReturn(Optional.of(taskToStart));
        when(orderTaskGateway.findByServiceOrderId(serviceOrderId))
                .thenReturn(List.of(alreadyStartedTask, taskToStart));

        useCase.updateStatus(serviceOrderId, taskId, "INICIADO");

        verify(orderTaskGateway).save(taskToStart);
        verifyNoInteractions(publisher);
    }

    @Test
    @DisplayName("Deve rejeitar INICIADO quando o core-service informa que a OS ainda não está pronta pra execução")
    void updateStatus_ShouldReject_WhenOrderNotReadyForExecution() {
        ServiceData service = defaultService();
        OrderTask task = OrderTask.create(serviceOrderId, service, List.of());
        UUID taskId = task.getId();

        when(coreServiceGateway.isReadyForExecution(serviceOrderId)).thenReturn(false);
        when(orderTaskGateway.findById(serviceOrderId, taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> useCase.updateStatus(serviceOrderId, taskId, "INICIADO"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("não está pronta para execução");

        verify(orderTaskGateway, never()).save(any());
        verifyNoInteractions(publisher);
    }

    @Test
    @DisplayName("Não deve publicar execucao.finalizada se ainda houver outra tarefa pendente")
    void updateStatus_ShouldNotPublish_WhenOtherTasksStillPending() {
        ServiceData service = defaultService();
        OrderTask taskToFinish = OrderTask.create(serviceOrderId, service, List.of());
        taskToFinish.start();
        UUID taskId = taskToFinish.getId();

        OrderTask stillPendingTask = OrderTask.create(serviceOrderId, defaultService(), List.of());

        when(orderTaskGateway.findById(serviceOrderId, taskId)).thenReturn(Optional.of(taskToFinish));
        when(orderTaskGateway.findByServiceOrderId(serviceOrderId))
                .thenReturn(List.of(taskToFinish, stillPendingTask));

        useCase.updateStatus(serviceOrderId, taskId, "FINALIZADO");

        verifyNoInteractions(publisher);
    }

    @Test
    @DisplayName("Deve publicar execucao.finalizada quando a última tarefa pendente é finalizada")
    void updateStatus_ShouldPublishExecucaoFinalizada_WhenLastTaskFinishes() {
        ServiceData service = defaultService();
        OrderTask task = OrderTask.create(serviceOrderId, service, List.of());
        task.start();
        UUID taskId = task.getId();

        when(orderTaskGateway.findById(serviceOrderId, taskId)).thenReturn(Optional.of(task));
        when(orderTaskGateway.findByServiceOrderId(serviceOrderId)).thenReturn(List.of(task));

        useCase.updateStatus(serviceOrderId, taskId, "FINALIZADO");

        verify(publisher).publishExecucaoFinalizada(serviceOrderId);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao atualizar status de tarefa inexistente")
    void updateStatus_ShouldThrowNotFound_WhenTaskDoesNotExist() {
        UUID taskId = UUID.randomUUID();
        when(orderTaskGateway.findById(serviceOrderId, taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.updateStatus(serviceOrderId, taskId, "INICIADO"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Deve dar baixa nos materiais da tarefa quando ela é finalizada")
    void updateStatus_ShouldConsumeTaskMaterials_WhenFinished() {
        ServiceData service = defaultService();
        UUID materialId = UUID.randomUUID();
        OrderTask task = OrderTask.create(serviceOrderId, service,
                List.of(new OrderTaskMaterialSnapshot(materialId, "Óleo 5W30", 2)));
        task.start();
        UUID taskId = task.getId();

        when(orderTaskGateway.findById(serviceOrderId, taskId)).thenReturn(Optional.of(task));
        when(orderTaskGateway.findByServiceOrderId(serviceOrderId)).thenReturn(List.of(task));

        useCase.updateStatus(serviceOrderId, taskId, "FINALIZADO");

        verify(stockReservationGateway).consume(serviceOrderId,
                List.of(new MaterialQuantity(materialId, 2)));
    }

    @Test
    @DisplayName("Não deve chamar baixa de estoque se a tarefa não tem materiais")
    void updateStatus_ShouldNotConsume_WhenTaskHasNoMaterials() {
        ServiceData service = defaultService();
        OrderTask task = OrderTask.create(serviceOrderId, service, List.of());
        task.start();
        UUID taskId = task.getId();

        when(orderTaskGateway.findById(serviceOrderId, taskId)).thenReturn(Optional.of(task));
        when(orderTaskGateway.findByServiceOrderId(serviceOrderId)).thenReturn(List.of(task));

        useCase.updateStatus(serviceOrderId, taskId, "FINALIZADO");

        verifyNoInteractions(stockReservationGateway);
    }

    @Test
    @DisplayName("Deve publicar execucao.falhou ao reportar falha na execução")
    void reportExecutionFailure_ShouldPublishEvent() {
        String motivo = "Problema maior que o diagnosticado";

        useCase.reportExecutionFailure(serviceOrderId, motivo);

        verify(publisher).publishExecucaoFalhou(serviceOrderId, motivo);
    }
}
